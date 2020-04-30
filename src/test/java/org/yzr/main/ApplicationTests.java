package org.yzr.main;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.yzr.dao.PackageDao;
import org.yzr.model.Package;
import org.yzr.service.PackageService;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {
    @Resource
    private PackageService packageService;

    @Resource
    private PackageDao packageDao;

    @Test
    public void contextLoads() {

    }

    @Test
    public void testSave() {
        Package aPackage = new Package();
        aPackage.setName("升学e网通");
        aPackage.setBundleID("org.yzr.test");
        aPackage.setVersion("6.9.7");
        this.packageService.save(aPackage);
    }

    @Test
    public void testFindTop(){
       Package aPackage= this.packageDao.findFirstByBigVersionAndEnvironmentOrderByCreateTimeDesc("6.4","sit");
        System.out.println(aPackage.getCreateTime());
    }

    @Test
    public void testFindTopByService(){
        Package aPackage=this.packageService.findTopPackageByEnvAndBigV("6.4","sit");
        System.out.println(aPackage.getCreateTime());
    }
}
