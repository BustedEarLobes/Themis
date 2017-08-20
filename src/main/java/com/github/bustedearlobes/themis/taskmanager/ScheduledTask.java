package com.github.bustedearlobes.themis.taskmanager;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.bustedearlobes.themis.exceptions.EntityNotFoundException;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
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
    
    protected Guild getGuildById(String guildId) {
        Guild guild = getJDA().getGuildById(guildId);
        if(guild == null) {
            throw new EntityNotFoundException("Could not find group from id " + guildId);
        }
        return guild;
    }
    
    private Member getMemberById(String memberId, Guild guild) {
        Member member = guild.getMemberById(memberId);
        if(member == null) {
            throw new EntityNotFoundException("Could not find member from id "
                                            + memberId
                                            + " in guild "
                                            + guild.getName());
        }
        return member;
    }
    
    private TextChannel getTextChannelById(String textChannelId, Guild guild) {
        TextChannel textChannel = guild.getTextChannelById(textChannelId);
        if(textChannel == null) {
            throw new EntityNotFoundException("Could not find textChannel from id " 
                                            + textChannelId
                                            + " in guild "
                                            + guild.getName());
        }
        return textChannel;
    }
    
    private User getUserById(String userId) {
        User user = jda.getUserById(userId);
        if(user == null) {
            throw new EntityNotFoundException("Could not find user from id " + userId);
        }
        return user;
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