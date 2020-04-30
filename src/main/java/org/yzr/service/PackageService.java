package org.yzr.service;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.yzr.dao.PackageDao;
import org.yzr.model.Package;
import org.yzr.utils.ImageUtils;
import org.yzr.utils.PathManager;
import org.yzr.utils.parser.ParserClient;
import org.yzr.vo.AppViewModel;
import org.yzr.vo.PackageViewModel;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class PackageService {

    @Resource
    private PackageDao packageDao;
    @Resource
    private PathManager pathManager;

    public Package buildPackage(String filePath) {
        Package aPackage = ParserClient.parse(filePath);
        try {
            String env = aPackage.getEnvironment();
            String environment = env.equals("prod") ? "" : env + "_";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
            String createTime = sdf.format(new Date(aPackage.getCreateTime()));

            String fileName = "meiju_" + environment + aPackage.getVersion() + "_"
                    + aPackage.getBuildVersion() + "_" + createTime + "." + FilenameUtils.getExtension(filePath);
            // 更新文件名
            aPackage.setFileName(fileName);

            String packagePath = PathManager.getFullPath(aPackage);
            String tempIconPath = PathManager.getTempIconPath(aPackage);
            String iconPath = packagePath + File.separator + "icon.png";
            String sourcePath = packagePath + File.separator + fileName;

            // 拷贝图标
            ImageUtils.resize(tempIconPath, iconPath, 192, 192);
            // 源文件
            FileUtils.copyFile(new File(filePath), new File(sourcePath));

            // 删除临时图标
            FileUtils.forceDelete(new File(tempIconPath));
            // 源文件
            FileUtils.forceDelete(new File(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return aPackage;
    }

    @Transactional
    public Package save(Package aPackage) {
        return this.packageDao.save(aPackage);
    }

    @Transactional
    public Package get(String id) {
        Package aPackage = this.packageDao.findById(id).get();
        return aPackage;
    }

    @Transactional
    public PackageViewModel findById(String id) {
        Package aPackage = this.packageDao.findById(id).get();
        PackageViewModel viewModel = new PackageViewModel(aPackage, this.pathManager);
        return viewModel;
    }

    @Transactional
    public void deleteById(String id) {
        Package aPackage = this.packageDao.findById(id).get();
        if (aPackage != null) {
            this.packageDao.deleteById(id);
            String path = PathManager.getFullPath(aPackage);
            PathManager.deleteDirectory(path);
        }
    }

    @Transactional
    public PackageViewModel findTopPackage(String bigVersion, String environment) {
        Package aPackage = this.packageDao.findFirstByBigVersionAndEnvironmentOrderByCreateTimeDesc(bigVersion, environment);
        PackageViewModel viewModel = new PackageViewModel(aPackage, this.pathManager);
        return viewModel;
    }

    @Transactional
    public Package findTopPackageByEnvAndBigV(String bigVersion, String environment) {
        if (environment != null && bigVersion != null) {
            return this.packageDao.findFirstByBigVersionAndEnvironmentOrderByCreateTimeDesc(bigVersion, environment);
        }
        return null;
    }

    @Transactional
    public Package findTopPackageByEnvAndBigVOrPackageId(String bigVersion, String environment, String packageId) {
        if (packageId != null) {
            return this.packageDao.findById(packageId).get();
        } else if (environment != null && bigVersion != null) {
            return this.packageDao.findFirstByBigVersionAndEnvironmentOrderByCreateTimeDesc(bigVersion, environment);
        }
        return null;
    }

    @Transactional
    public List<Package> findPackageListByEnvAndBigv(String bigVersion, String environment) {
        if (environment != null && bigVersion != null) {
            return this.packageDao.findAllByBigVersionAndEnvironment(bigVersion, environment);
        }
        return null;
    }


}
