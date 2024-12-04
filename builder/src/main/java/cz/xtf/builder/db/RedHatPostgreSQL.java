package cz.xtf.builder.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import cz.xtf.builder.builders.pod.PersistentVolumeClaim;
import cz.xtf.core.image.Image;

public class RedHatPostgreSQL extends AbstractSQLDatabase {

    public static final String DEFAULT_SYMBOLIC_NAME = "POSTGRESQL";
    private static final String DEFAULT_DATA_DIR = "/var/lib/pgsql/data";

    // default env variables for the Red Hat image
    private static final Map<String, String> DEFAULT_VARS = new HashMap<String, String>() {
        {
            put("POSTGRESQL_MAX_CONNECTIONS", "100");
            put("POSTGRESQL_SHARED_BUFFERS", "16MB");
            put("POSTGRESQL_MAX_PREPARED_TRANSACTIONS", "90");
            // Temporary workaround for https://github.com/sclorg/postgresql-container/issues/297
            // Increase the "set_passwords.sh" timeout from the default 60s to 300s to give the
            // PostgreSQL server chance properly to start under high OCP cluster load
            put("PGCTLTIMEOUT", "300");
        }
    };

    // env variables names for the Red Hat image
    private static final String DEFAULT_POSTGRESQL_USER_ENV_VAR = "POSTGRESQL_USER";
    private static final String DEFAULT_POSTGRESQL_DATABASE_ENV_VAR = "POSTGRESQL_DATABASE";

    private String postgresqlUserEnvVar;
    private String postgresqlDatabaseEnvVar;
    private Map<String, String> vars;
    private List<String> args;

    public RedHatPostgreSQL(RedHatPostgreSQLBuilder builder) {
        super(
                (builder.symbolicName == null || builder.symbolicName.isEmpty())
                        ? DEFAULT_SYMBOLIC_NAME
                        : builder.symbolicName,
                (builder.dataDir == null || builder.dataDir.isEmpty())
                        ? DEFAULT_DATA_DIR
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
        postgresqlUserEnvVar = DEFAULT_POSTGRESQL_USER_ENV_VAR;
        postgresqlDatabaseEnvVar = DEFAULT_POSTGRESQL_DATABASE_ENV_VAR;
        this.vars = builder.vars;
        if (this.vars == null) {
            this.vars = DEFAULT_VARS;
        }
        this.args = builder.args;
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
    public List<String> getImageArgs() {
        return args;
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
                "psql -h 127.0.0.1 -U $POSTGRESQL_USER -q -d $POSTGRESQL_DATABASE -c 'SELECT 1'",
                5,
                "psql -h 127.0.0.1 -U $POSTGRESQL_USER -q -d $POSTGRESQL_DATABASE -c 'SELECT 1'",
                10,
                10);
    }

    @Override
    public String toString() {
        return "RedHatPostgreSQL";
    }

    @Override
    protected String getJDBCConnectionStringPattern() {
        return "jdbc:postgresql://%s:%s/%s";
    }

    @Override
    public Map<String, String> getImageVariables() {
        Map<String, String> vars = super.getImageVariables();
        vars.put("POSTGRESQL_MAX_CONNECTIONS", "100");
        vars.put("POSTGRESQL_SHARED_BUFFERS", "16MB");
        vars.put("POSTGRESQL_MAX_PREPARED_TRANSACTIONS", "90");
        // Temporary workaround for https://github.com/sclorg/postgresql-container/issues/297
        // Increase the "set_passwords.sh" timeout from the default 60s to 300s to give the
        // RedHatPostgreSQL server chance properly to start under high OCP cluster load
        vars.put("PGCTLTIMEOUT", "300");
        return vars;
    }

    public static class RedHatPostgreSQLBuilder {
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

        public RedHatPostgreSQLBuilder withArgs(List<String> args) {
            this.args = args;
            return this;
        }

        public RedHatPostgreSQLBuilder withConfigureEnvironment(boolean configureEnvironment) {
            this.configureEnvironment = configureEnvironment;
            return this;
        }

        public RedHatPostgreSQLBuilder withDataDir(String dataDir) {
            this.dataDir = dataDir;
            return this;
        }

        public RedHatPostgreSQLBuilder withDbName(String dbName) {
            this.dbName = dbName;
            return this;
        }

        public RedHatPostgreSQLBuilder withDeploymentConfigName(Supplier<String> deploymentConfigName) {
            this.deploymentConfigName = deploymentConfigName;
            return this;
        }

        public RedHatPostgreSQLBuilder withEnvVarPrefix(Supplier<String> envVarPrefix) {
            this.envVarPrefix = envVarPrefix;
            return this;
        }

        public RedHatPostgreSQLBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public RedHatPostgreSQLBuilder withPvc(PersistentVolumeClaim pvc) {
            this.pvc = pvc;
            return this;
        }

        public RedHatPostgreSQLBuilder withSymbolicName(String symbolicName) {
            this.symbolicName = symbolicName;
            return this;
        }

        public RedHatPostgreSQLBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public RedHatPostgreSQLBuilder withVars(Map<String, String> vars) {
            this.vars = vars;
            return this;
        }

        public RedHatPostgreSQLBuilder withWithLivenessProbe(boolean withLivenessProbe) {
            this.withLivenessProbe = withLivenessProbe;
            return this;
        }

        public RedHatPostgreSQLBuilder withWithReadinessProbe(boolean withReadinessProbe) {
            this.withReadinessProbe = withReadinessProbe;
            return this;
        }

        public RedHatPostgreSQLBuilder withWithStartupProbe(boolean withStartupProbe) {
            this.withStartupProbe = withStartupProbe;
            return this;
        }

        public RedHatPostgreSQL build() {
            RedHatPostgreSQL postgreSQL = new RedHatPostgreSQL(this);
            return postgreSQL;
        }
    }
}