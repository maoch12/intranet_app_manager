package org.yzr.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import org.yzr.model.Package;

import java.util.List;
import java.util.Optional;

public interface PackageDao extends CrudRepository<Package, String> {

    Package findFirstByBigVersionAndEnvironmentOrderByCreateTimeDesc(String bigVersion, String environment);

    List<Package> findAllByBigVersionAndEnvironment(String bigVersion, String environment);
}
