package edu.stanford.futuredata.macrobase.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.futuredata.macrobase.analysis.summary.Explanation;
import edu.stanford.futuredata.macrobase.pipeline.*;
import edu.stanford.futuredata.macrobase.datamodel.DataFrame;
import edu.stanford.futuredata.macrobase.sql.MacroBaseSQLSession;
import edu.stanford.futuredata.macrobase.util.MacroBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.HashMap;

import spark.Filter;

import static spark.Spark.*;

public class RestServer {
    private static Logger log = LoggerFactory.getLogger(RestServer.class);

    private static MacroBaseSQLSession session = new MacroBaseSQLSession();

    /* Changing response headers to allow POST access from separately hosted UI */
    private static final HashMap<String, String> corsHeaders = new HashMap<String, String>();
    static {
        corsHeaders.put("Access-Control-Allow-Origin", "*");
    }

    public static void main(String[] args) {
        RestServer.apply();

        post("/sql", RestServer::processSQLQuery, RestServer::toJsonString);
        post("/query", RestServer::processBasicBatchQuery, RestServer::toJsonString);
        post("/rows", RestServer::getRows, RestServer::toJsonString);

        exception(Exception.class, (exception, request, response) -> {
            log.error("An exception occurred: ", exception);
        });
    }

    public final static void apply() {
        Filter filter = new Filter() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                corsHeaders.forEach((key, value) -> {
                    response.header(key, value);
                });
            }
        };
        after(filter);
    }

    private static DataFrame processSQLQuery(Request request, Response response) throws MacroBaseException {
        return session.executeQuery(request.queryString());
    }

    public static Explanation processBasicBatchQuery(
            Request req, Response res
    ) throws Exception {

        res.type("application/json");
        PipelineConfig conf = PipelineConfig.fromJsonString(req.body());
        Pipeline p = PipelineUtils.createPipeline(conf);
        return p.results();
    }

    public static DataFrame getRows(
            Request req, Response res
    ) throws Exception {
        //res.type()
        PipelineConfig conf = PipelineConfig.fromJsonString(req.body());
        Pipeline p = PipelineUtils.createPipeline(conf);
        DataFrame df = p.getRows();
        return df;
    }

    public static String toJsonString(Object o) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(o);
    }
}
