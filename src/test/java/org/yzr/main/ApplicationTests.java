package org.yzr.main;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.yzr.dao.PackageDao;
import org.yzr.model.App;
import org.yzr.model.Package;
import org.yzr.service.PackageService;

import javax.annotation.Resource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public void testfind(){
        App app=new App();
        app.setId("4028ab0071ecce980171eccf8e1e0000");
        this.packageDao.findAllByApp(app);
    }

    @Test
    public void testRegex(){
        String line = "6.10.0.3";
        String pattern = "^(\\d+)(\\.)(\\d+)(\\.)(\\d+)";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m = r.matcher(line);
        if (m.find( )) {
            System.out.println("Found value: " + m.group(0) );
        } else {
            System.out.println("NO MATCH");
        }
    }
}
