package org.yzr.utils.parser;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.IconFace;
import org.apache.commons.io.FileUtils;
import org.yzr.model.Package;
import org.yzr.utils.PathManager;

import java.io.File;
import java.util.UUID;

public class APKParser implements PackageParser {
    @Override
    public Package parse(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) return null;
            ApkFile apkFile = new ApkFile(file);
            long currentTimeMillis = System.currentTimeMillis();
            Package aPackage = new Package();
            aPackage.setSize(file.length());
            ApkMeta meta = apkFile.getApkMeta();
            aPackage.setName(meta.getName());
            String buildVersion = meta.getVersionCode() + "";
            if (buildVersion.length() < 1) {
                buildVersion = meta.getPlatformBuildVersionCode();
            }
            setVersionAndEnv(meta, aPackage);
            aPackage.setBuildVersion(buildVersion);
            aPackage.setBundleID(meta.getPackageName());
            aPackage.setMinVersion(meta.getMinSdkVersion());
            aPackage.setPlatform("android");
            aPackage.setCreateTime(currentTimeMillis);
            int iconCount = apkFile.getAllIcons().size();
            if (iconCount > 0) {
                IconFace icon = apkFile.getAllIcons().get(iconCount - 1);
                String iconPath = PathManager.getTempIconPath(aPackage);
                File iconFile = new File(iconPath);
                FileUtils.writeByteArrayToFile(iconFile, icon.getData());
            }
            apkFile.close();
            return aPackage;
        } catch (Exception e) {

        }
        return null;
    }

    private void setVersionAndEnv(ApkMeta meta, Package aPackage) {
        String versionName = meta.getVersionName();
        if (versionName.length() < 1) {
            versionName = meta.getPlatformBuildVersionName();
        }
        String version = "";
        String environment = "";
        if (versionName.contains("_")) {
            String[] ver = versionName.trim().split("_");
            environment = ver[0];
            version = ver[1];
        } else {
            environment = "prod";
            version = versionName;
        }
        aPackage.setVersion(version);
        aPackage.setBigVersion(version.substring(0,5));
        aPackage.setEnvironment(environment);
    }

}
