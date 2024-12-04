package cz.xtf.builder.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import cz.xtf.builder.builders.pod.PersistentVolumeClaim;
import cz.xtf.core.image.Image;

public class OfficialPostgreSQL extends AbstractSQLDatabase {
    private static final String DEFAULT_SYMBOLIC_NAME = "POSTGRESQL";

    // data directory for the Official Docker PostgreSQL image
    private static final String OFFICIAL_IMAGE_DATA_DIR = "/var/lib/postgresql/data";
    private static final String OFFICIAL_IMAGE_PGDATA_DIR = "/var/lib/postgresql/data/pgdata";

    // env variables names for the Official Docker PostgreSQL image
    private static final String OFFICIAL_IMAGE_POSTGRESQL_USER_ENV_VAR = "POSTGRES_USER";
    private static final String OFFICIAL_IMAGE_POSTGRESQL_DATABASE_ENV_VAR = "POSTGRES_DB";
    private static final String OFFICIAL_IMAGE_POSTGRES_PASSWORD_ENV_VAR = "POSTGRES_PASSWORD";

    // default env variables for for the Official Docker PostgreSQL image
    private static final Map<String, String> OFFICIAL_IMAGE_DEFAULT_VARS = new HashMap<String, String>() {
        {
            // Temporary workaround for https://github.com/sclorg/postgresql-container/issues/297
            // Increase the "set_passwords.sh" timeout from the default 60s to 300s to give the
            // PostgreSQL server chance properly to start under high OCP cluster load
            put("PGCTLTIMEOUT", "300");
        }
    };

    // default command arguments for the Official Docker PostgreSQL image
    private static final List<String> OFFICIAL_IMAGE_DEFAULT_ARGS = new ArrayList<String>() {
        {
            add("-c");
            add("shared_buffers=16MB");
            add("-c");
            add("max_connections=100");
            add("-c");
            add("max_prepared_transactions=90");
        }
    };

    private String postgresqlUserEnvVar;
    private String postgresqlDatabaseEnvVar;
    private Map<String, String> vars;
    private List<String> args;
    private String dataDir;
    private String serviceAccount;
    private String pgData;

    private OfficialPostgreSQL(PostgreSQLBuilder builder) {
        super(
                (builder.symbolicName == null || builder.symbolicName.isEmpty())
                        ? DEFAULT_SYMBOLIC_NAME
                        : builder.symbolicName,
                (builder.dataDir == null || builder.dataDir.isEmpty())
                        ? OFFICIAL_IMAGE_DATA_DIR
                        : builder.dataDir,
                builder.pvc,
                builder.username,
                builder.password,
                builder.dbName,
                builder.configureEnvironment,
                builder.withLivenessProbe,
                builder.withReadinessProbe,
                builder.withStartupProbe,
                builder.deploymentConfigName,
                builder.envVarPrefix);
        postgresqlUserEnvVar = OFFICIAL_IMAGE_POSTGRESQL_USER_ENV_VAR;
        postgresqlDatabaseEnvVar = OFFICIAL_IMAGE_POSTGRESQL_DATABASE_ENV_VAR;
        this.vars = builder.vars;
        if (this.vars == null) {
            this.vars = OFFICIAL_IMAGE_DEFAULT_VARS;
        }
        this.args = builder.args;
        if (this.args == null) {
            this.args = OFFICIAL_IMAGE_DEFAULT_ARGS;
        }
        this.dataDir = (builder.dataDir == null || builder.dataDir.isEmpty())
                ? OFFICIAL_IMAGE_DATA_DIR
                : builder.dataDir;
        this.serviceAccount = builder.serviceAccount;
        this.pgData = (builder.pgData == null || builder.pgData.isEmpty())
                ? OFFICIAL_IMAGE_PGDATA_DIR
                : builder.pgData;
    }

    public void setPostgresqlUserEnvVar(String postgresqlUserEnvVar) {
        this.postgresqlUserEnvVar = postgresqlUserEnvVar;
    }

    public void setPostgresqlDatabaseEnvVar(String postgresqlDatabaseEnvVar) {
        this.postgresqlDatabaseEnvVar = postgresqlDatabaseEnvVar;
    }

    public void setVars(Map<String, String> vars) {
        this.vars = vars;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    @Override
    public String getImageName() {
        return Image.resolve("postgresql").getUrl();
    }

    @Override
    public int getPort() {
        return 5432;
    }

    protected ProbeSettings getProbeSettings() {
        return new ProbeSettings(300,
                String.valueOf(getPort()),
                5,
                String.format(
                        "psql -h 127.0.0.1 -U $%s -q -d $%s -c 'SELECT 1'",
                        postgresqlUserEnvVar,
                        postgresqlDatabaseEnvVar),
                5,
                String.format(
                        "psql -h 127.0.0.1 -U $%s -q -d $%s -c 'SELECT 1'",
                        postgresqlUserEnvVar,
                        postgresqlDatabaseEnvVar),
                10,
                10);
    }

    @Override
    public String toString() {
        return "OfficialPostgreSQL";
    }

    @Override
    protected String getJDBCConnectionStringPattern() {
        return "jdbc:postgresql://%s:%s/%s";
    }

    @Override
    public Map<String, String> getImageVariables() {
        Map<String, String> vars;
        vars = new HashMap<>();
        vars.put(OFFICIAL_IMAGE_POSTGRESQL_USER_ENV_VAR, getUsername());
        vars.put(OFFICIAL_IMAGE_POSTGRES_PASSWORD_ENV_VAR, getPassword());
        vars.put(OFFICIAL_IMAGE_POSTGRESQL_DATABASE_ENV_VAR, getDbName());
        vars.put("PGDATA", this.pgData);
        vars.putAll(this.vars);
        return vars;
    }

    @Override
    public List<String> getImageArgs() {
        return args;
    }

    @Override
    public String getServiceAccount() {
        return serviceAccount;
    }

    public static class PostgreSQLBuilder {
        private String symbolicName;
        private String dataDir;
        private PersistentVolumeClaim pvc;
        private String username;
        private String password;
        private String dbName;
        private boolean configureEnvironment = true;
        private boolean withLivenessProbe;
        private boolean withReadinessProbe;
        private boolean withStartupProbe;
        private Map<String, String> vars;
        private List<String> args;
        private Supplier<String> deploymentConfigName;
        private Supplier<String> envVarPrefix;
        private String serviceAccount;
        private String pgData;

        public PostgreSQLBuilder withArgs(List<String> args) {
            this.args = args;
            return this;
        }

        public PostgreSQLBuilder withConfigureEnvironment(boolean configureEnvironment) {
            this.configureEnvironment = configureEnvironment;
            return this;
        }

        public PostgreSQLBuilder withDataDir(String dataDir) {
            this.dataDir = dataDir;
            return this;
        }

        public PostgreSQLBuilder withDbName(String dbName) {
            this.dbName = dbName;
            return this;
        }

        public PostgreSQLBuilder withDeploymentConfigName(Supplier<String> deploymentConfigName) {
            this.deploymentConfigName = deploymentConfigName;
            return this;
        }

        public PostgreSQLBuilder withEnvVarPrefix(Supplier<String> envVarPrefix) {
            this.envVarPrefix = envVarPrefix;
            return this;
        }

        public PostgreSQLBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public PostgreSQLBuilder withPvc(PersistentVolumeClaim pvc) {
            this.pvc = pvc;
            return this;
        }

        public PostgreSQLBuilder withSymbolicName(String symbolicName) {
            this.symbolicName = symbolicName;
            return this;
        }

        public PostgreSQLBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public PostgreSQLBuilder withVars(Map<String, String> vars) {
            this.vars = vars;
            return this;
        }

        public PostgreSQLBuilder withWithLivenessProbe(boolean withLivenessProbe) {
            this.withLivenessProbe = withLivenessProbe;
            return this;
        }

        public PostgreSQLBuilder withWithReadinessProbe(boolean withReadinessProbe) {
            this.withReadinessProbe = withReadinessProbe;
            return this;
        }

        public PostgreSQLBuilder withWithStartupProbe(boolean withStartupProbe) {
            this.withStartupProbe = withStartupProbe;
            return this;
        }

        public PostgreSQLBuilder withServiceAccount(String serviceAccount) {
            this.serviceAccount = serviceAccount;
            return this;
        }

        public PostgreSQLBuilder withPgData(String pgData) {
            this.pgData = pgData;
            return this;
        }

        public OfficialPostgreSQL build() {
            OfficialPostgreSQL postgreSQL = new OfficialPostgreSQL(this);
            return postgreSQL;
        }
    }
}
