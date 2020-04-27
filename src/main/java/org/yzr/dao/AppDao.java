package org.yzr.dao;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.yzr.model.App;

import java.util.Optional;

public interface AppDao extends CrudRepository<App, String> {

    @Query("select a from App a where a.bundleID=:bundleID and a.platform=:platform")
    public App get(@Param("bundleID") String bundleID, @Param("platform") String platform);

    @Query("select a from App a where a.shortCode=:shortCode")
    public App findByShortCode(@Param("shortCode") String shortCode);

    @Override
    @Query("select a from App a order by a.currentPackage.createTime desc ")
    Iterable<App> findAll();

    @NotNull
    @Query("select a from App a where a.id=:id")
    public Optional<App> findById(@Param("id") String id);
}
