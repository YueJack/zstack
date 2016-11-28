package org.zstack.storage.boss;

import org.zstack.core.config.GlobalConfig;
import org.zstack.core.config.GlobalConfigDefinition;
import org.zstack.core.config.GlobalConfigValidation;

/**
 * Created by XXPS-PC1 on 2016/10/27.
 */
@GlobalConfigDefinition
public class BossGlobalConfig {
    public static final String CATEGORY = "bossStorage";

    @GlobalConfigValidation(numberGreaterThan = 0)
    public static GlobalConfig IMAGE_CACHE_CLEANUP_INTERVAL = new GlobalConfig(CATEGORY, "imageCache.cleanup.interval");

}
