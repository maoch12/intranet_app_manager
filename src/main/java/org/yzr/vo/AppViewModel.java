package org.yzr.vo;


import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.yzr.model.App;
import org.yzr.model.Package;
import org.yzr.utils.PathManager;

import java.io.File;
import java.io.IOException;
import java.util.*;


@Getter
public class AppViewModel {

    private String id;

    private String name;

    private String platform;

    private String bundleID;

    private String icon;

    private String version;

    private String bigVersion;

    private String buildVersion;

    private String minVersion;

    private String shortCode;

    private String installPath;

    private String environment;

    private String log;

    private String bundleName;

    private List<PackageViewModel> packageList;

    private PackageViewModel currentPackage;


    /***
     * 初始化是否加载列表
     * @param app
     * @param pathManager
     * @param loadList
     */
    public AppViewModel(App app, PathManager pathManager, boolean loadList) {
        this.id = app.getId();
        this.platform = app.getPlatform();
        this.bundleID = app.getBundleID();
        this.icon = PathManager.getRelativePath(app.getCurrentPackage()) + "icon.png";
        this.log = findLogText(PathManager.getFullPath(app.getCurrentPackage()) + "log.txt");
        Package aPackage = findPackageById(app, null);
        this.version = aPackage.getVersion();
        this.bigVersion = aPackage.getBigVersion();
        this.environment = aPackage.getEnvironment();
        this.buildVersion = aPackage.getBuildVersion();
        this.shortCode = app.getShortCode();
        this.name = app.getName();
        this.bundleName =app.getBundleName();
        this.installPath = pathManager.getBaseURL(false) + "app/"+app.getBundleName()+"/"  + aPackage.getEnvironment() + "/" + aPackage.getBigVersion() + "/";
        this.minVersion = aPackage.getMinVersion();
        this.currentPackage = new PackageViewModel(aPackage, pathManager);
        if (loadList) {
            // 排序
            this.packageList = sortPackages(app.getPackageList(), pathManager);
        }
    }

    public AppViewModel(App app, PathManager pathManager, String packageId) {
        this.id = app.getId();
        this.platform = app.getPlatform();
        this.bundleID = app.getBundleID();
        this.icon = PathManager.getRelativePath(app.getCurrentPackage()) + "icon.png";
        this.log = findLogText(PathManager.getFullPath(app.getCurrentPackage()) + "log.txt");
        Package aPackage = findPackageById(app, packageId);
        this.version = aPackage.getVersion();
        this.bigVersion = aPackage.getBigVersion();
        this.environment = aPackage.getEnvironment();
        this.buildVersion = aPackage.getBuildVersion();
        this.shortCode = app.getShortCode();
        this.name = app.getName();
        this.bundleName =app.getBundleName();
        this.installPath = pathManager.getBaseURL(false) + "app/" +app.getBundleName()+"/" + aPackage.getEnvironment() + "/" + aPackage.getBigVersion() + "/";
        this.minVersion = aPackage.getMinVersion();
        this.currentPackage = new PackageViewModel(aPackage, pathManager);
    }

    public AppViewModel(App app, PathManager pathManager, List<Package> packages, Package topPackge) {
        this.id = app.getId();
        this.platform = app.getPlatform();
        this.bundleID = app.getBundleID();
        this.icon = PathManager.getRelativePath(topPackge) + "icon.png";
        this.log = findLogText(PathManager.getFullPath(topPackge) + "log.txt");
        this.version = topPackge.getVersion();
        this.bigVersion = topPackge.getBigVersion();
        this.environment = topPackge.getEnvironment();
        this.buildVersion = topPackge.getBuildVersion();
        this.shortCode = app.getShortCode();
        this.name = app.getName();
        this.bundleName =app.getBundleName();
        this.installPath = pathManager.getBaseURL(false) + "app/" +app.getBundleName()+"/" + topPackge.getEnvironment() + "/" + topPackge.getBigVersion() + "/";
        this.minVersion = topPackge.getMinVersion();
        this.currentPackage = new PackageViewModel(topPackge, pathManager);
        this.packageList = sortPackages(packages, pathManager);
    }

    //加载预览页所以需要的数据
    public AppViewModel(App app, PathManager pathManager, Package aPackage) {
        this.id = app.getId();
        this.platform = app.getPlatform();
        this.bundleID = app.getBundleID();
        this.icon = PathManager.getRelativePath(aPackage) + "icon.png";
        this.log = findLogText(PathManager.getFullPath(aPackage) + "log.txt");
        this.version = aPackage.getVersion();
        this.bigVersion = aPackage.getBigVersion();
        this.environment = aPackage.getEnvironment();
        this.buildVersion = aPackage.getBuildVersion();
        this.shortCode = app.getShortCode();
        this.name = app.getName();
        this.bundleName =app.getBundleName();
        this.installPath = pathManager.getBaseURL(false) + "app/"+app.getBundleName()+"/"  + aPackage.getEnvironment() + "/" + aPackage.getBigVersion() + "/";
        this.minVersion = aPackage.getMinVersion();
        this.currentPackage = new PackageViewModel(aPackage, pathManager);
    }

    private static Package findPackageById(App app, String id) {
        if (id != null) {
            for (Package aPackage : app.getPackageList()) {
                if (aPackage.getId().equals(id)) {
                    return aPackage;
                }
            }
        }
        return app.getCurrentPackage();
    }

    private static String findLogText(String path) {
        File logFile = new File(path);
        try {
            return FileUtils.readFileToString(logFile, "UTF-8");
        } catch (IOException e) {
            return "日志获取失败，联系打包人员，检查是否有上传日志";
        }
    }


    private static List<PackageViewModel> sortPackages(List<Package> packages, PathManager pathManager) {
        // 排序
        List<PackageViewModel> packageViewModels = new ArrayList<>();
        for (Package aPackage : packages) {
            PackageViewModel packageViewModel = new PackageViewModel(aPackage, pathManager);
            packageViewModels.add(packageViewModel);
        }
        packageViewModels.sort((o1, o2) -> {
            if (o1.getCreateTime() > o2.getCreateTime()) {
                return -1;
            }
            return 1;
        });
        return packageViewModels;
    }
}
