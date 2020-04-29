package org.yzr.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import org.yzr.model.Package;

import java.util.List;

public interface PackageDao extends CrudRepository<Package, String> {

    @Query("SELECT p from Package p where p.bigVersion=:bigVersion and p.environment=:environment")
    List<Package> findAllByBigVAndEnv(@Param("bigVersion") String bigVersion,
                                      @Param("environment") String environment);

//    Package findFirstByBigVersionAndEnvironmentOrderByCreateTimeDesc(@Param("bigVersion") String bigVersion,
//                                                                     @Param("environment") String environment);

     Package findFirstByBigVersionAndEnvironmentOrderByCreateTimeDesc(String bigVersion, String environment);

    List<Package> findAllByBigVersionAndEnvironment(@Param("bigVersion") String bigVersion,
                                                    @Param("environment") String environment);
}
