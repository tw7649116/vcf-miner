package edu.mayo.ve.range;

import edu.mayo.concurrency.exceptions.ProcessTerminatedException;
import edu.mayo.concurrency.workerQueue.Task;
import edu.mayo.concurrency.workerQueue.WorkerLogic;
import edu.mayo.util.Tokens;
import edu.mayo.ve.dbinterfaces.DatabaseImplMongo;
import edu.mayo.ve.resources.MetaData;
import edu.mayo.ve.resources.RangeQueryInterface;

import edu.mayo.ve.util.IOUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Created by m102417 on 3/6/15.
 */
public class RangeWorker implements WorkerLogic {

    private static final Logger log = Logger.getLogger(RangeWorker.class);

    public static final String RANGE_FILE = "range_file";
    public static final String INTERVAL_NAME = "interval_name";
    public static final String UPDATE_FREQ = "update_freq";

    @Override
    public Object compute(Task t) throws ProcessTerminatedException {
        File intervalFile = null;
        if(verboseMode){
            log.info("Starting compute to update ranges... verboseMode:" + verboseMode );
        }
        HashMap<String,String> context = (HashMap) t.getCommandContext();
        String workspace = context.get(Tokens.KEY);
        String loadfile = context.get(RANGE_FILE);
        String name = context.get(INTERVAL_NAME);
        Integer n = Integer.parseInt(context.get(UPDATE_FREQ));
        MetaData meta = new MetaData();
        RangeQueryInterface rangeQ = new RangeQueryInterface();

        try {
            //update the workspace to include the new range set as a flag (file intervals)
            intervalFile = new File(loadfile);

            BufferedReader br = new BufferedReader(new FileReader(intervalFile));
            //if(verboseMode){
                log.info("Updating the range");
            //}
            DatabaseImplMongo dim = new DatabaseImplMongo();

            // store total number of intervals
            int numIntervals = IOUtils.getLineCount(intervalFile);
            dim.setMetadataValue(workspace, "annotation_count_total", numIntervals);
            // initialize
            dim.setMetadataValue(workspace, "annotation_count_current", 0);

            dim.bulkUpdate(workspace, new FileIterator(br), n, name);

            //need to flag the workspace as ready
            if(verboseMode){ log.info("Flagging the Workspace as ready"); }

        }catch (Throwable e){
            //todo: need to log the errors to some sort of file and produce another REST call so that the UI can get the errors!
            log.error("Error updating ranges", e);
            throw new ProcessTerminatedException();
        }finally {
            //delete the temp file if it is not null and it exists
            if( intervalFile != null  &&  intervalFile.exists() )
                intervalFile.delete();
            meta.flagAsReady(workspace);
        }

        return null;
    }

    private boolean verboseMode = false;
    public void setLogStackTrace(boolean on){
        verboseMode = on;
    }

    public boolean isVerboseMode() {
        return verboseMode;
    }

    public void setVerboseMode(boolean verboseMode) {
        this.verboseMode = verboseMode;
        if(verboseMode){ log.info("Verbose Mode is now turned on in RangeWorker"); }
    }
}
