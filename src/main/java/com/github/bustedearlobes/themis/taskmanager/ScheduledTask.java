package com.github.bustedearlobes.themis.taskmanager;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.bustedearlobes.themis.Themis;
import com.github.bustedearlobes.themis.exceptions.EntityNotFoundException;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public abstract class ScheduledTask extends ListenerAdapter implements Runnable, Serializable {
    private static final long serialVersionUID = 2L;
    private static final Logger LOG = Logger.getLogger("Themis");
    
    private long numberOfRuns = 0;
    private long periodicity;
    private long repeat;
    private TaskState state;
    
    private transient long timeOfNextRun;
    private transient Themis themis;

    
    public ScheduledTask(long delay, long periodicity, TimeUnit timeUnit, long repeat) {
        this.periodicity = timeUnit.toMillis(periodicity);
        this.timeOfNextRun = System.currentTimeMillis() + timeUnit.toMillis(delay);
        this.repeat = repeat;
        state = TaskState.QUEUED;
    }

    @Override
    public final void run() {
        setState(TaskState.RUNNING);
        getJDA().addEventListener(this);
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
            getJDA().removeEventListener(this);
        }
        if(!completedAllRuns()) {
            setState(TaskState.QUEUED);
        } else {
            setState(TaskState.CLEANUP);
        }
    }
    
    public boolean isState(TaskState state) {
        synchronized(this.state) {
            return (this.state == state);
        }
    }
    
    public TaskState getState() {
        synchronized(state) {
            return state;
        }
    }
    
    protected boolean cleanUpTask() {
        boolean success = false;
        try {
            cleanUpJDAChanges();
            success = true;
        } catch(Exception e) {
            LOG.log(Level.SEVERE, "Error while cleaning up task.", e);
        }
        setState(TaskState.DEAD);
        return success;
    }
    
    protected final long getTimeUntilNextRun() {
        return timeOfNextRun - System.currentTimeMillis();
    }
    
    protected final void incrementRun() {
        timeOfNextRun = System.currentTimeMillis() + periodicity;
        if(repeat != Long.MAX_VALUE) {
            numberOfRuns++;
        }
    }
    
    protected final boolean taskIsReady() {
        return (System.currentTimeMillis() >= timeOfNextRun);
    }

    protected final boolean completedAllRuns() {
        return (numberOfRuns > repeat);
    }
    
    protected final JDA getJDA() {
        if(themis == null || themis.getJDA() == null) {
            LOG.log(Level.SEVERE, "Themis or JDA are null in task. Cannot get JDA.");
        }
        return themis.getJDA();
    }
    
    protected final Themis getThemis() {
        if(themis == null) {
            LOG.log(Level.SEVERE, "Themis not set in task");
        }
        return themis;
    }
    
    protected final void setThemis(Themis themis) {
        this.themis = themis;
    }
    
    protected final Guild getGuildById(String guildId) {
        Guild guild = getJDA().getGuildById(guildId);
        if(guild == null) {
            throw new EntityNotFoundException("Could not find group from id " + guildId);
        }
        return guild;
    }
    
    protected final Member getMemberById(String memberId, Guild guild) {
        Member member = guild.getMemberById(memberId);
        if(member == null) {
            throw new EntityNotFoundException("Could not find member from id "
                                            + memberId
                                            + " in guild "
                                            + guild.getName());
        }
        return member;
    }
    
    protected final TextChannel getTextChannelById(String textChannelId, Guild guild) {
        TextChannel textChannel = guild.getTextChannelById(textChannelId);
        if(textChannel == null) {
            throw new EntityNotFoundException("Could not find textChannel from id " 
                                            + textChannelId
                                            + " in guild "
                                            + guild.getName());
        }
        return textChannel;
    }
    
    protected final User getUserById(String userId) {
        User user = getJDA().getUserById(userId);
        if(user == null) {
            throw new EntityNotFoundException("Could not find user from id " + userId);
        }
        return user;
    }
    
    protected void cleanUpJDAChanges() { }
    
    
    private void setState(TaskState state) {
        synchronized(this.state) {
            this.state = state;
        }
    }
    
    public abstract String getName();
    protected abstract void runTask();
}
