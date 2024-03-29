package org.yzr.vo;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.yzr.model.Package;
import org.yzr.service.PackageService;
import org.yzr.utils.PathManager;
import org.yzr.utils.ipa.PlistGenerator;

import javax.annotation.Resource;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Getter
public class PackageViewModel {
    private String downloadURL;
    private String safeDownloadURL;
    private String iconURL;
    private String installURL;
    private String previewURL;
    private String id;
    private String version;
    private String bigVersion;
    private String environment;
    private String bundleID;
    private String name;
    private long createTime;
    private String buildVersion;
    private String displaySize;
    private String displayTime;
    private boolean iOS;
    private String type;
    private String log;
    private String bundleName;
    private int count;
    private List<String> devices;
    private int deviceCount;

    public PackageViewModel(Package aPackage, PathManager pathManager) {
        this.downloadURL = pathManager.getBaseURL(false) + "p/" + aPackage.getId();
        this.safeDownloadURL = pathManager.getBaseURL(true) + "p/" + aPackage.getId();
        this.iconURL = pathManager.getPackageResourceURL(aPackage, true) + "icon.png";
        String logPath = PathManager.getFullPath(aPackage) + "log.txt";
        try {
            this.log = FileUtils.readFileToString(new File(logPath), "UTF-8");
        } catch (IOException e) {
            this.log = "日志获取失败，联系打包人员，检查是否有上传日志";
        }
        this.id = aPackage.getId();
        this.version = aPackage.getVersion();
        this.environment = aPackage.getEnvironment();
        this.bundleID = aPackage.getBundleID();
        this.name = aPackage.getName();
        this.bigVersion = aPackage.getBigVersion();
        this.createTime = aPackage.getCreateTime();
        this.buildVersion = aPackage.getBuildVersion();
        this.displaySize = String.format("%.2f MB", aPackage.getSize() / (1.0F * FileUtils.ONE_MB));
        Date updateTime = new Date(this.createTime);
        String displayTime = (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(updateTime);
        this.displayTime = displayTime;
        this.bundleName=aPackage.getApp().getBundleName();
        this.count=aPackage.getCount();
        if (aPackage.getPlatform().equals("ios")) {
            this.iOS = true;
            //下载ios
            String url = pathManager.getBaseURL(true) + "m/" + aPackage.getId();
//            String url = pathManager.getPackageResourceURL(aPackage, true) + "manifest.plist";
            try {
                this.installURL = "itms-services://?action=download-manifest&url=" + URLEncoder.encode(url, "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (aPackage.getPlatform().equals("android")) {
            this.iOS = false;
            this.installURL = pathManager.getPackageResourceURL(aPackage, false) + aPackage.getFileName();
        }
        this.previewURL = pathManager.getBaseURL(false) + "app/" + aPackage.getApp().getBundleName() + "/"
               + aPackage.getEnvironment() + "/" + aPackage.getBigVersion() + "?id=" + aPackage.getId();
        if (this.isIOS()) {
            if (aPackage.getProvision() == null) {
                this.type = "内测版";
            } else {
                if (aPackage.getProvision().isEnterprise()) {
                    this.type = "企业版";
                } else {
                    if ("AdHoc".equalsIgnoreCase(aPackage.getProvision().getType())) {
                        this.type = "内测版";
                    } else {
                        this.type = "商店版";
                    }
                    this.deviceCount = aPackage.getProvision().getDeviceCount();
                    if (aPackage.getProvision().getDeviceCount() > 0) {
                        this.devices = Arrays.asList(aPackage.getProvision().getDevices());
                    }
                }
            }
        } else {
            this.type = "内测版";
        }
    }

}
