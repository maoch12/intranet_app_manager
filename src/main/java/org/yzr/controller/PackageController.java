package org.yzr.controller;


import net.glxn.qrgen.javase.QRCode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.env.Environment;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.yzr.model.App;
import org.yzr.model.Package;
import org.yzr.service.AppService;
import org.yzr.service.PackageService;
import org.yzr.utils.PathManager;
import org.yzr.utils.ipa.PlistGenerator;
import org.yzr.utils.webhook.WebHookClient;
import org.yzr.vo.AppViewModel;
import org.yzr.vo.PackageViewModel;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class PackageController {
    @Resource
    private AppService appService;
    @Resource
    private PackageService packageService;
    @Resource
    private PathManager pathManager;
    @Resource
    private Environment environment;

    /**
     * 预览页
     *
     * @param code
     * @param request
     * @return
     */
    @GetMapping("/s/{code}")
    public String get(@PathVariable("code") String code, HttpServletRequest request) {
        String id = request.getParameter("id");
        AppViewModel viewModel = this.appService.findByCode(code, id);
        request.setAttribute("app", viewModel);
        request.setAttribute("ca_path", this.pathManager.getCAPath());
        request.setAttribute("basePath", this.pathManager.getBaseURL(false));
        return "install";
    }

    //美居预览页
    @GetMapping("/app/{bundleName}/{env}/{bigV}")
    public String getByEnv(@PathVariable("bundleName") String bundleName,
                           @PathVariable("env") String env, @PathVariable("bigV") String bigV,
                           HttpServletRequest request) {
        String boundId = transferToBoundId(bundleName);
        String id = request.getParameter("id");
        App app = this.appService.findApp(boundId);
        Package aPackage = this.packageService.findTopPackageByEnvAndBigVOrPackageId(app, bigV, env, id);
        AppViewModel viewModel = this.appService.findPackageByEnvAndBigV(app, aPackage);
        request.setAttribute("app", viewModel);
        request.setAttribute("ca_path", this.pathManager.getCAPath());
        request.setAttribute("basePath", this.pathManager.getBaseURL(false));

        return "install";
    }

    //历史版本列表页
    @GetMapping("/apps/{boundName}/{env}/{bigV}")
    public String getAppsByEnv(@PathVariable("boundName") String boundName,
                               @PathVariable("env") String env, @PathVariable("bigV") String bigV,
                               HttpServletRequest request) {
        String boundId = transferToBoundId(boundName);
        App app = this.appService.findApp(boundId);
        List<Package> packageList = this.packageService.findPackageListByEnvAndBigv(app, bigV, env);
        Package topPackage = this.packageService.findTopPackageByEnvAndBigVOrPackageId(app, bigV, env, "");
        AppViewModel appViewModel = this.appService.findPackagesByEnvAndBigv(app, packageList, topPackage);
        request.setAttribute("package", appViewModel);
        request.setAttribute("apps", appViewModel.getPackageList());
        return "list";
    }


    /**
     * 设备列表
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/devices/{id}")
    public String devices(@PathVariable("id") String id, HttpServletRequest request) {
        PackageViewModel viewModel = this.packageService.findById(id);
        request.setAttribute("app", viewModel);
        return "devices";
    }

    /**
     * 安装教程
     *
     * @param platform
     * @param request
     * @return
     */
    @GetMapping("/guide/{platform}")
    public String guide(@PathVariable("platform") String platform, HttpServletRequest request) {
        request.setAttribute("platform", platform);
        return "guide";
    }

    /**
     * 上传包
     *
     * @param file
     * @param request
     * @return
     */
    @RequestMapping("/app/upload")
    @ResponseBody
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
                                      HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        try {
            String filePath = transfer(file);
            Package aPackage = this.packageService.buildPackage(filePath, "");
            App app = this.appService.getByPackage(aPackage);
            app.getPackageList().add(aPackage);
            app.setCurrentPackage(aPackage);
            app.setBundleName(transferToBoundName(aPackage.getBundleID()));
            aPackage.setApp(app);
            app = this.appService.save(app);
            //兼容旧版本
//            savePlist(app.getCurrentPackage());
            // URL
            String codeURL = this.pathManager.getBaseURL(false) + "p/code/" + app.getCurrentPackage().getId();
            // 发送WebHook消息
            WebHookClient.sendMessage(app, pathManager);
            map.put("code", codeURL);
            map.put("success", true);
        } catch (Exception e) {
            map.put("success", false);
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 命令行发送请求，便于集成到Jenkins上
     *
     * @param appFile apk文件或者ipa文件
     * @param logFile 日志文件
     */
    @RequestMapping("/app/uploads")
    @ResponseBody
    public Map<String, Object> uploads(@RequestParam("app") MultipartFile appFile, @RequestParam("log")
            MultipartFile logFile,
                                       HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        try {
            String logPath = transfer(logFile);
            String appPath = transfer(appFile);
            Package aPackage = this.packageService.buildPackage(appPath, logPath);
            App app = this.appService.getByPackage(aPackage);
            app.getPackageList().add(aPackage);
            app.setCurrentPackage(aPackage);
            app.setBundleName(transferToBoundName(aPackage.getBundleID()));
            aPackage.setApp(app);
            aPackage.setCount(0);
            app = this.appService.save(app);
            //如果是ios包，还需要生成plist文件
//            savePlist(app.getCurrentPackage());
            // URL
            String codeURL = this.pathManager.getBaseURL(false) + "p/code/" + app.getCurrentPackage().getId();
            // 发送WebHook消息
            WebHookClient.sendMessage(app, pathManager);
            map.put("code", codeURL);
            map.put("success", true);
        } catch (Exception e) {
            map.put("success", false);
            e.printStackTrace();
        }
        return map;
    }

    private void savePlist(Package aPackage){
        String packagePath = PathManager.getFullPath(aPackage);
        String plistPath=packagePath + File.separator+"manifest.plist";
        //如果是ios，则生成plist文件
        PackageViewModel viewModel = new PackageViewModel(aPackage, this.pathManager);
        if (viewModel!=null&&viewModel.isIOS()){
            PlistGenerator.generate(viewModel,plistPath);
        }
    }

    /**
     * 下载文件源文件(ipa 或 apk)
     *
     * @param id
     * @param response
     */
    @RequestMapping("/p/{id}")
    public void download(@PathVariable("id") String id, HttpServletResponse response) {
        try {
            Package aPackage = this.packageService.get(id);
            int count = aPackage.getCount();
            String path = PathManager.getFullPath(aPackage) + aPackage.getFileName();
            File file = new File(path);
            if (file.exists()) { //判断文件父目录是否存在
                response.setContentType("application/force-download");
                // 文件名称转换
                response.setHeader("Content-Disposition", "attachment;fileName=" + aPackage.getFileName());

                byte[] buffer = new byte[1024];
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer);
                    i = bis.read(buffer);
                    count++;
                }
                aPackage.setCount(count);
                bis.close();
                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  统计下载量；下载一次，计数器加1
     * @param packageId 下载的包
     */
    @PostMapping("/app/updateCount")
    public void updateCount(@RequestParam("packageId") String packageId, HttpServletResponse response) {
        Package aPackage = this.packageService.get(packageId);
        int count = aPackage.getCount() + 1;
        aPackage.setCount(count);
        this.packageService.save(aPackage);
    }

    /**
     * 获取 manifest
     *
     * @param id
     * @param response
     */
    @RequestMapping("/m/{id}")
    public void getManifest(@PathVariable("id") String id, HttpServletResponse response) {
        try {
            PackageViewModel viewModel = this.packageService.findById(id);

            if (viewModel != null && viewModel.isIOS()) {
                response.setContentType("application/force-download");
                response.setHeader("Content-Disposition", "attachment;fileName=manifest.plist");
                Writer writer = new OutputStreamWriter(response.getOutputStream());
                PlistGenerator.generate(viewModel, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取包二维码
     *
     * @param id
     * @param response
     */
    @RequestMapping("/p/code/{id}")
    public void getQrCode(@PathVariable("id") String id, HttpServletResponse response) {
        try {
            PackageViewModel viewModel = this.packageService.findById(id);
            if (viewModel != null) {
                response.setContentType("image/png");
                QRCode.from(viewModel.getPreviewURL()).withSize(250, 250).writeTo(response.getOutputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除包
     *
     * @param id
     * @return
     */
    @RequestMapping("/p/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteById(@PathVariable("id") String id) {
        Map<String, Object> map = new HashMap<>();
        try {
            this.packageService.deleteById(id);
            map.put("success", true);
        } catch (Exception e) {
            map.put("success", false);
        }
        return map;
    }

    /**
     * 转存文件
     *
     * @param srcFile
     * @return
     */
    private String transfer(MultipartFile srcFile) {
        try {
            // 获取文件后缀
            String fileName = srcFile.getOriginalFilename();
            String ext = FilenameUtils.getExtension(fileName);
            // 生成文件名
            String newFileName = UUID.randomUUID().toString() + "." + ext;
            // 转存到 tmp
            String destPath = FileUtils.getTempDirectoryPath() + File.separator + newFileName;
            destPath = destPath.replaceAll("//", "/");
            srcFile.transferTo(new File(destPath));
            return destPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String transferToBoundId(String pathVariable) {
        return environment.getProperty("com.midea.package." + pathVariable);
    }

    private String transferToBoundName(String bundleID) {
        return environment.getProperty(bundleID);
    }
}
