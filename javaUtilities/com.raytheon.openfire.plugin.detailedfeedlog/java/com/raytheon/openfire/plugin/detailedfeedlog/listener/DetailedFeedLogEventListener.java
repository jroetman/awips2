/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.openfire.plugin.detailedfeedlog.listener;

import java.util.Queue;
import java.util.TimerTask;

import org.jivesoftware.openfire.MessageRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.muc.MUCEventListener;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.TaskEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Message.Type;

import com.raytheon.openfire.plugin.detailedfeedlog.DetailedFeedLogPlugin;
import com.raytheon.openfire.plugin.detailedfeedlog.LogEntry;

/**
 * Listens to events and calls methods on whether a user enters a room (sends
 * the history) or whether a message is received (calls the plugin to log it if
 * it qualifies)
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 25, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class DetailedFeedLogEventListener implements MUCEventListener {

    private static Logger logger = LoggerFactory
            .getLogger(DetailedFeedLogEventListener.class);

    private MessageRouter router = null;

    // we do not want todo anything with command packets
    private static final String COMMAND_START = "[[COMMAND";

    // this tag will tell CAVE that this is a history message
    private static final String HISTORY_START = "[[HISTORY]]";

    /**
     * When the occupant joins, we want to send out the history to them. This
     * only happens in the permanent feed room.
     */
    @Override
    public void occupantJoined(final JID roomJID, final JID user,
            final String nickname) {
        if (isLoggedRoom(roomJID)) {
            logger.info(user + " joining logged room " + roomJID);
            TimerTask messageTask = new TimerTask() {
                @Override
                public void run() {
                    UserManager userManager = UserManager.getInstance();
                    Queue<LogEntry> log = DetailedFeedLogPlugin.getLog(roomJID
                            .getNode());

                    for (LogEntry entry : log) {
                        Message message = new Message();
                        message.setTo(roomJID + "/" + nickname);
                        String usr = entry.getUsername();
                        String[] info = usr.split("\\|");
                        User sendUser = null;
                        try {
                            sendUser = userManager
                                    .getUser(info[0].split("@")[0]);
                        } catch (UserNotFoundException e) {
                            logger.error(usr + " not found", e);
                        }
                        String site = info[1];
                        // set all the necessary values in the message to be
                        // sent out
                        message.setTo(user);
                        message.setFrom(roomJID + "/" + nickname);
                        String body = HISTORY_START + entry.getDate().getTime()
                                + "|" + sendUser + "|" + site + "|"
                                + entry.getMessage();
                        message.setBody(body);
                        message.setType(Type.groupchat);

                        router.route(message);
                        logger.info("Routed message : " + message.getBody()
                                + " from " + sendUser + " to " + user);
                    }
                }
            };
            TaskEngine.getInstance().schedule(messageTask, 0);
        }
    }

    /**
     * We want to log here since this intercepts all packets in the room
     */
    @Override
    public void messageReceived(JID roomJID, JID user, String nickname,
            Message message) {
        if (isLoggedRoom(roomJID)) {
            if (message.getBody().startsWith(COMMAND_START) == false) {
                DetailedFeedLogPlugin.log(message, roomJID.getNode());
            }
        }
    }

    /**
     * Determine if this is the room that we want to log for here
     * 
     * @param room
     * @return
     */
    private boolean isLoggedRoom(JID roomJID) {
        MUCRoom mucRoom = XMPPServer.getInstance().getMultiUserChatManager()
                .getMultiUserChatService(roomJID)
                .getChatRoom(roomJID.getNode());
        boolean logEnabled = mucRoom.isLogEnabled();
        return logEnabled;
    }

    @Override
    public void roomCreated(JID roomJID) {
        boolean logEnabled = isLoggedRoom(roomJID);
        logger.info("Logs for " + roomJID.getNode() + " are "
                + (logEnabled ? "enabled" : "disabled"));
        // create the queue in memory when the room is created
        DetailedFeedLogPlugin.addRoomToLog(roomJID.getNode());
    }

    @Override
    public void roomDestroyed(JID roomJID) {
        // remove the queue from the log when the room is destroyed
        DetailedFeedLogPlugin.removeRoomFromLog(roomJID.getNode());
    }

    @Override
    public void occupantLeft(JID roomJID, JID user) {
        // the only action that matters is the occupant joined action
    }

    @Override
    public void nicknameChanged(JID roomJID, JID user, String oldNickname,
            String newNickname) {
        // we don't care about if the nickname is changed
    }

    @Override
    public void privateMessageRecieved(JID toJID, JID fromJID, Message message) {
        // unnecessary
    }

    @Override
    public void roomSubjectChanged(JID roomJID, JID user, String newSubject) {
        // unnecessary
    }

    /**
     * @param router
     *            the router to set
     */
    public void setRouter(MessageRouter router) {
        this.router = router;
    }
}
