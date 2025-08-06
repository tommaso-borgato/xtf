package cz.xtf.builder.db;

import java.util.HashMap;
import java.util.Map;

import cz.xtf.builder.builders.pod.PersistentVolumeClaim;
import cz.xtf.core.image.Image;

public class Oracle19c extends AbstractOracle {
    private static final String DB_NAME = "ORCLPDB1";
    private static final String ORACLE_PWD = "adminPassword";
    private static final String APP_USER = "test";
    private static final String APP_USER_PASSWORD = "test";
    protected static final String DATA_DIR = "/opt/oracle/oradata";

    public Oracle19c() {
        super(APP_USER, APP_USER_PASSWORD, DB_NAME, DATA_DIR);
    }

    public Oracle19c(PersistentVolumeClaim pvc) {
        super(APP_USER, APP_USER_PASSWORD, DB_NAME, DATA_DIR, pvc);
    }

    public Oracle19c(PersistentVolumeClaim pvc, boolean withLivenessProbe, boolean withReadinessProbe) {
        super(APP_USER, APP_USER_PASSWORD, DB_NAME, DATA_DIR, pvc, withLivenessProbe, withReadinessProbe);
    }

    @Override
    public String getImageName() {
        return Image.resolve("oracle19c").getUrl();
    }

    @Override
    public Map<String, String> getImageVariables() {
        //Oracle database image doesn't create custom database and user on setup, thus we don't need the default ImageVariable
        Map<String, String> vars = new HashMap<>();
        vars.put("TARGET_PDB", DB_NAME);
        vars.put("APP_USER", APP_USER);
        vars.put("APP_USER_PASSWORD", APP_USER_PASSWORD);
        vars.put("ORACLE_PWD", ORACLE_PWD);
        return vars;
    }

    public static String getSysPassword() {
        return ORACLE_PWD;
    }
}
