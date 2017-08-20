package com.github.bustedearlobes.themis.taskmanager;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public abstract class ScheduledTask extends ListenerAdapter implements Runnable, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger("Themis");

    private long periodicity;
    private long repeat;
    private long numberOfRuns;
    private long timeOfNextRun;
    private transient JDA jda;
    
    public ScheduledTask(long delay, long periodicity, long repeat) {
        this.periodicity = periodicity;
        this.repeat = repeat;
        this.timeOfNextRun = System.currentTimeMillis() + delay;
        this.numberOfRuns = 0;
    }

    protected boolean taskIsReady() {
        return (System.currentTimeMillis() >= timeOfNextRun);
    }

    protected boolean isExpired() {
        return (numberOfRuns > repeat);
    }
    
    protected JDA getJDA() {
        if(jda == null) {
            LOG.log(Level.SEVERE, "JDA not set in task");
        }
        return jda;
    }
    
    protected void setJDA(JDA jda) {
        this.jda = jda;
    }

    @Override
    public final void run() {
        jda.addEventListener(this);
        try {
            runTask();
        } catch(Exception e) {
            StackTraceElement[] st = e.getStackTrace();
            if(st.length > 0) {
                StackTraceElement lastStackTrace = st[st.length - 1];
                LOG.logp(Level.WARNING, lastStackTrace.getClassName(), lastStackTrace.getMethodName(),
                        "Exception occured in scheduled task.", e);
            } else {
                LOG.log(Level.WARNING, "Exception occured in scheduled task.", e);
            }
        } finally {
            jda.removeEventListener(this);
        }
        timeOfNextRun = System.currentTimeMillis() + periodicity;
        if(repeat != Long.MAX_VALUE) {
            numberOfRuns++;
        }
    }

    protected abstract void runTask();
}
