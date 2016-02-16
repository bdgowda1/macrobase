package macrobase.runtime.resources;

import macrobase.analysis.BaseAnalyzer;
import macrobase.conf.ConfigurationException;
import macrobase.conf.MacroBaseConf;
import macrobase.ingest.PostgresLoader;
import macrobase.ingest.SQLLoader;

import java.sql.SQLException;
import java.util.ArrayList;

abstract public class BaseResource {
    protected final MacroBaseConf conf;
    public BaseResource(MacroBaseConf conf) {
       this.conf = conf;
    }

    protected SQLLoader getLoader() throws ConfigurationException, SQLException {
        // by default, REST calls may not have these defined.
        conf.set(MacroBaseConf.ATTRIBUTES, new ArrayList<>());
        conf.set(MacroBaseConf.LOW_METRICS, new ArrayList<>());
        conf.set(MacroBaseConf.HIGH_METRICS, new ArrayList<>());
        return new PostgresLoader(conf);
    }
}
