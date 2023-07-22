package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WhatsappRepository {

    Map<String, User> userDB = new HashMap<>();

    Map<User, List<Message>> userMessagesList = new HashMap<>();

    Map<Group, List<User>> groupUserList = new HashMap<>();

    Map<Integer, Message> messageDB = new LinkedHashMap<>();

    Map<Group, List<Message>> groupMessages = new HashMap<>();

    int groupCount = 0;

    int messageIndex = 0;

    public String createUser(String name, String mobile) throws Exception {

        if(userDB.containsKey(mobile)) {
            throw new Exception("User already exists");
        }
        User user = new User(name,mobile);
        userDB.put(mobile, user);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {

        String groupName = null;
        if(users.size()>2) {
            groupName = "Group "+ ++groupCount;
        }
        else {
            groupName = users.get(1).getName();
        }
        Group group = new Group(groupName, users.size());
        groupUserList.put(group, users);
        return group;
    }

    public int createMessage(String content) {

        int messageID = ++messageIndex;
        Message message = new Message(messageID, content, new Date());
        messageDB.put(messageID, message);
        return messageID;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {

        if(!groupUserList.containsKey(group)) {
            throw new Exception("Group does not exist");
        }
        boolean userFound = false;
        for(User user : groupUserList.get(group)) {
            if(user==sender) {
                userFound = true;
                break;
            }
        }
        if(!userFound) {
            throw new Exception("You are not allowed to send message");
        }

        if(groupMessages.containsKey(group)) {
            groupMessages.get(group).add(message);
        }
        else {
            List<Message> messageList = new ArrayList<>();
            messageList.add(message);
            groupMessages.put(group, messageList);
        }

        if(userMessagesList.containsKey(sender)) {
            userMessagesList.get(sender).add(message);
        }
        else {
            List<Message> messageList = new ArrayList<>();
            messageList.add(message);
            userMessagesList.put(sender, messageList);
        }

        return groupMessages.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {

        if(!groupUserList.containsKey(group)) {
            throw new Exception("Group does not exist");
        }
        if(approver!=groupUserList.get(group).get(0)) {
            throw new Exception("Approver does not have rights");
        }
        boolean userFound = false;
        for(User user1 : groupUserList.get(group)) {
            if(user==user1) {
                userFound = true;
                break;
            }
        }
        if(!userFound) {
            throw new Exception("User is not a participant");
        }

        List<User> userList = groupUserList.get(group);
        userList.remove(user);
        userList.set(0,user);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception {

        Group group =  null;
        for(Group group1 : groupUserList.keySet()) {
            for (User user1 : groupUserList.get(group1)) {
                if(user==user1) {
                    if(groupUserList.get(group1).get(0)==user) {
                        throw new Exception("Cannot remove admin");
                    }
                    group = group1;
                    break;
                }
            }
        }
        if(group==null) {
            throw new Exception("User not found");
        }
        for(Message message : userMessagesList.get(user)) {
            messageDB.remove(message.getId());
            groupMessages.get(group).remove(message);
        }
        userMessagesList.remove(user);
        groupUserList.get(group).remove(user);
        userDB.remove(user.getMobile());
        return groupUserList.get(group).size()+groupMessages.get(group).size()+messageDB.size();
    }

    public String findMessage(Date start, Date end, int k) throws Exception {

        List<Message> messageList = new ArrayList<>();
        for(Message message : messageDB.values()) {
            if(message.getTimestamp().compareTo(start)>1 && message.getTimestamp().compareTo(end)<1) {
                messageList.add(message);
            }
        }
        if(messageList.size()<k) {
            throw new Exception("K is greater than the number of messages");
        }
        return messageList.get(messageList.size()-k).getContent();
    }
}