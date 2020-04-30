package org.yzr.service;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.yzr.dao.AppDao;
import org.yzr.model.App;
import org.yzr.model.Package;
import org.yzr.utils.CodeGenerator;
import org.yzr.utils.PathManager;
import org.yzr.vo.AppViewModel;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AppService {
    @Resource
    private AppDao appDao;
    @Resource
    private PathManager pathManager;

    @Transactional
    public App save(App app) {
        App app1 = this.appDao.save(app);
        app1.getCurrentPackage();
        try {
            // 触发级联查询
            app1.getWebHookList().forEach(webHook -> {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return app1;
    }

    @Transactional
    public List<AppViewModel> findAll() {
        Iterable<App> apps = this.appDao.findAll();
        List<AppViewModel> list = new ArrayList<>();
        for (App app : apps) {
            AppViewModel appViewModel = new AppViewModel(app, this.pathManager, false);
            list.add(appViewModel);
        }
        return list;
    }

    @Transactional
    public AppViewModel getById(String appID) {
        Optional<App> optionalApp = this.appDao.findById(appID);
        App app = optionalApp.get();
        if (app != null) {
            app.getPackageList().forEach(aPackage -> {
            });
            AppViewModel appViewModel = new AppViewModel(app, this.pathManager, true);
            return appViewModel;
        }
        return null;
    }

    //TODO update code value to ios or andriod
    @Transactional
    public App getByPackage(Package aPackage) {
        App app = this.appDao.get(aPackage.getBundleID(), aPackage.getPlatform());
        if (app == null) {
            app = new App();
            BeanUtils.copyProperties(aPackage, app);
            String shortCode = app.getPlatform();
            app.setShortCode(shortCode);
        } else {
            app.setName(aPackage.getName());
            // 触发级联查询
            app.getPackageList().forEach(p -> {
            });
            app.getWebHookList().forEach(webHook -> {
            });
        }
        if (app.getPackageList() == null) {
            app.setPackageList(new ArrayList<>());
        }
        return app;
    }

    @Transactional
    public void deleteById(String id) {
        App app = this.appDao.findById(id).get();
        if (app != null) {
            this.appDao.deleteById(id);
            // 消除整个 APP 目录
            String path = PathManager.getAppPath(app);
            PathManager.deleteDirectory(path);
        }

    }

    /**
     * 通过 code 和 packageId 查询
     *
     * @param code
     * @param packageId
     * @return
     */
    @Transactional
    public AppViewModel findByCode(String code, String packageId) {
        App app = this.appDao.findByShortCode(code);
        AppViewModel viewModel = new AppViewModel(app, pathManager, packageId);
        return viewModel;
    }

    @Transactional
    public AppViewModel findPackageByEnvAndBigV(String code, Package aPackage) {
        App app = this.appDao.findByShortCode(code);
//        if (app != null) {
            AppViewModel viewModel = new AppViewModel(app, pathManager, aPackage);
            return viewModel;
//        }
//        return null;
    }

    @Transactional
    public AppViewModel findPackagesByEnvAndBigv(String code,List<Package> packages,Package topPackage){
        App app=this.appDao.findByShortCode(code);
        if (app!=null){
            AppViewModel viewModel=new AppViewModel(app,pathManager,packages,topPackage);
            return viewModel;
        }
        return null;
    }


}
