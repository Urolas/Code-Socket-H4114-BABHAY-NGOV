/**
 * LogManager.java
 * @authors Annie ABHAY, Sophanna NGOV
 */
package stream;

import java.io.*;
import java.lang.invoke.StringConcatFactory;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages the files and return the necessary information from these files
 */
public class LogManager {

    /**
     * Search the user's name on allUsers.txt
     * @param username
     * @return true or false if this User exist ( connected once before )
     */
    public static boolean userExist(String username){

        boolean found = false;

        //Find the name of the user in the user list
        try(FileReader fileReader = new FileReader("logFiles/allUsers.txt")) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if(line.equals(username)){
                    found = true;
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }

        return found;
    }

    /**
     * Search the group's name on allGroups.txt
     * @param groupName
     * @return true or false if the group exists/has been created
     */
    public static boolean groupExist(String groupName){

        boolean found = false;

        //Find the name of the user in the user list
        try(FileReader fileReader = new FileReader("logFiles/allGroups.txt")) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if(line.split(" ")[0].equals(groupName)){
                    found = true;
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }

        return found;
    }

    /**
     * Create a new log file for the user if the user didn't exist
     * @param username
     */
    //Check if user already exist if not create Log
    public static void findNameInFile(String username){

        boolean found = userExist(username);

        //if the user doesn't exist, create a Log file for them
        if(!found) {

            try (FileWriter fw = new FileWriter("logFiles/allUsers.txt",true)) {
                fw.write(username+"\n"); //appends the new username to the file
                fw.close();

                //and create the file
                File newFile = new File("logFiles/Log_"+username+".txt");
                newFile.createNewFile();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println(e);
            }
        }

    }

    /**
     * Add a new line(new message) on the log txt file
     * @param username
     * @param message
     */
    public static void writeOnUserLog(String username, String message){

        try (FileWriter fw = new FileWriter("logFiles/Log_"+username+".txt",true)) {
            fw.write(message+"\n"); //appends the new username to the file
            fw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * Read the log file and return the entire history
     * @param username
     * @return the entire history of this user personal log
     */
    public static String getHistory(String username){
        String log="";

        try(FileReader fileReader = new FileReader("logFiles/Log_"+username+".txt")) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log += line+"\n";
            }
        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }

        return log;
    }


    /**
     * Read the group's history and return the entire group log
     * @param groupName
     * @return the entire group history
     */
    public static String getHistoryGroup(String groupName){
        String log="";

        try(FileReader fileReader = new FileReader("logFiles/GroupLog_"+groupName+".txt")) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log += line+"\n";
            }
        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }

        return log;
    }


    /**
     * If group doesn't exist, create it
     * @param groupName
     * @param creator the name of the group owner
     * @return true if no problem, false if the group can't be created
     */
    public static boolean findGroupInFile(String groupName, String creator){

        boolean found = groupExist(groupName);

        //group doesn't exist
        if(!found) {

            try (FileWriter fw = new FileWriter("logFiles/allGroups.txt",true)) {
                fw.write(groupName+" "+creator+"\n"); //appends the new username to the file
                fw.close();

                //and create the file
                File newFile = new File("logFiles/GroupLog_"+groupName+".txt");
                newFile.createNewFile();
                return true;


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        return false;
    }


    /**
     * Check if the username is a group owner
     * @param groupName
     * @param username
     * @return true or false
     */
    public static boolean isGroupOwner(String groupName, String username){

        boolean owner = false;

        //Find the name of the user in the user list
        try(FileReader fileReader = new FileReader("logFiles/allGroups.txt")) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if(line.split(" ")[0].equals(groupName)){
                    if(line.split(" ")[1].equals(username)){
                        owner = true;
                    }else{
                        owner = false;
                    }
                    break;

                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }
        return owner;

    }


     /**
     * Add a member to the group
     * @param groupName
     * @param people List of members to add to the group
     * @return true if no problem, false if member can't be added
     */
    public static boolean addPeopleToGroup(String groupName, String[] people){

        boolean success = true;

        //add people to group

        try{
            File inputFile = new File("logFiles/allGroups.txt");
            File tempFile = new File("logFiles/myTempFile.txt");

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;
            String[] initialPeople= {};


            //Remove the old line
            while((currentLine = reader.readLine()) != null) {
                String trimmedLine = currentLine.trim();
                if(trimmedLine.split(" ")[0].equals(groupName)) {
                    initialPeople = trimmedLine.substring(trimmedLine.indexOf(groupName) + groupName.length() + 1).split(" ");
                    for (String name : people){ //if the person to be added is already on the List
                        if(belongToGroup(groupName,name,trimmedLine)){
                            tempFile.delete();
                            return false;
                        }
                    }
                    continue;
                }
                writer.write(currentLine + System.getProperty("line.separator"));
            }

            //Write a new line with the update
            String[] both = Arrays.copyOf(initialPeople, initialPeople.length + people.length);
            System.arraycopy(people, 0, both, initialPeople.length, people.length); //merge oldList and newList
            Arrays.stream(both).distinct().toArray(); //remove duplicate
            writer.write(groupName+" "+ Stream.of(both).collect(Collectors.joining(" "))+System.getProperty("line.separator"));


            writer.close();
            reader.close();
            inputFile.delete();
            success = tempFile.renameTo(inputFile);

        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }

        return success;

    }

    /**
     * Search for all the group this user belong to
     * @param username
     * @return the group names and members amount
     */
    public static String[] getUserGroups(String username){
        String group="";

        try(FileReader fileReader = new FileReader("logFiles/allGroups.txt")) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                String[] splitLine = line.split(" ");
                String groupName = splitLine[0];
                String[] people = line.substring(line.indexOf(groupName) + groupName.length() + 1).split(" ");
                if(Arrays.asList(people).contains(username)){
                    group+= groupName + " "+people.length +" ";
                }


            }
        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }

        return group.split(" ");
    }


    /**
     * Get the group members' usernames
     * @param groupName
     * @param username
     * @return the names of members
     */
    public static String getGroupMembers(String groupName, String username){
        String members = "";

        //Find the name of the user in the user list
        try(FileReader fileReader = new FileReader("logFiles/allGroups.txt")) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if(line.split(" ")[0].equals(groupName)){
                    String[] memTab = line.substring(line.indexOf(groupName) + groupName.length() + 1).split(" ");
                    memTab[0] = memTab[0] + "(owner)";
                    if(belongToGroup(groupName, username, line)) {
                        members = Stream.of(memTab).collect(Collectors.joining(", "));
                    }else{
                        members = null;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }
        return members;

    }

    /**
     * Search the list of members and find out if the mentioned username belong to this list
     * @param groupName
     * @param username
     * @param line A line from allGroups.txt
     * @return true or false
     */
    public static boolean belongToGroup(String groupName, String username, String line){
        String[] people = line.substring(line.indexOf(groupName) + groupName.length() + 1).split(" ");
        if(Arrays.asList(people).contains(username)){
            return true;
        }else{
            return false;
        }
    }


    /**
     * Remove a member from the group
     * @param groupName
     * @param people the list of people to be removed
     * @return true if removal completed, false if these members can't be removed
     */
    public static boolean removePeopleFromGroup(String groupName, String[] people){

        boolean success = true;

        try{
            File inputFile = new File("logFiles/allGroups.txt");
            File tempFile = new File("logFiles/myTempFile.txt");

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;
            String[] initialPeople= {};


            //Remove the old line
            while((currentLine = reader.readLine()) != null) {
                String trimmedLine = currentLine.trim();
                if(trimmedLine.split(" ")[0].equals(groupName)) {
                    initialPeople = trimmedLine.substring(trimmedLine.indexOf(groupName) + groupName.length() + 1).split(" ");
                    for (String name : people){ //if the person to be removed isn't in the member list
                        if(!belongToGroup(groupName,name,trimmedLine)){
                            tempFile.delete();
                            return false;
                        }else if(isGroupOwner(groupName,name)){
                            tempFile.delete();
                            return false;
                        }
                    }

                    continue;
                }
                writer.write(currentLine + System.getProperty("line.separator"));
            }

            //Write a new line with the update
            String newName ="";
            for (String name : initialPeople){
                if(!Arrays.asList(people).contains(name)){
                    newName+=name+" ";
                }
            }
            writer.write(groupName+" "+newName+System.getProperty("line.separator"));

            writer.close();
            reader.close();
            inputFile.delete();
            success = tempFile.renameTo(inputFile);

        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }

        return success;

    }

    /**
     * Remove the mentioned username from the group
     * @param groupName
     * @param username
     * @return true if removal completed, false if errors occurred
     */
    public static boolean leaveGroup(String groupName, String username){

        boolean success = true;

        try{
            File inputFile = new File("logFiles/allGroups.txt");
            File tempFile = new File("logFiles/myTempFile.txt");

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;
            String[] initialPeople= {};


            //Remove the old line
            while((currentLine = reader.readLine()) != null) {
                String trimmedLine = currentLine.trim();
                if(trimmedLine.split(" ")[0].equals(groupName)) {
                    initialPeople = trimmedLine.substring(trimmedLine.indexOf(groupName) + groupName.length() + 1).split(" ");
                    if(!belongToGroup(groupName,username,trimmedLine)){
                        tempFile.delete();
                        return false;
                    }else if(isGroupOwner(groupName,username)){
                        tempFile.delete();
                        return false;
                    }

                    continue;
                }
                writer.write(currentLine + System.getProperty("line.separator"));
            }

            //Write a new line with the update
            String newName ="";
            for (String name : initialPeople){
                if(!name.equals(username)){
                    newName+=name+" ";
                }
            }
            writer.write(groupName+" "+newName+System.getProperty("line.separator"));

            writer.close();
            reader.close();
            inputFile.delete();
            success = tempFile.renameTo(inputFile);

        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }

        return success;
    }

    /**
     * Add a new line of the GroupLog txt file
     * @param groupName
     * @param message the lines(message) to be added to the log
     */
    public static void writeOnGroupLog(String groupName, String message){

        try (FileWriter fw = new FileWriter("logFiles/GroupLog_"+groupName+".txt",true)) {
            fw.write(message+"\n"); //appends the new username to the file
            fw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * Delete a group and its log file
     * @param groupName
     * @return true if no problem, false if the group can't be deleted
     */
    public static boolean deleteGroup(String groupName){
        boolean success=true;
        File GroupLog = new File("logFiles/GroupLog_"+groupName+".txt");

        try{
            File inputFile = new File("logFiles/allGroups.txt");
            File tempFile = new File("logFiles/myTempFile.txt");

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;


            //Remove the old line
            while((currentLine = reader.readLine()) != null) {
                String trimmedLine = currentLine.trim();
                if(trimmedLine.split(" ")[0].equals(groupName)) {
                    continue;
                }
                writer.write(currentLine + System.getProperty("line.separator"));
            }


            writer.close();
            reader.close();
            inputFile.delete();
            success = tempFile.renameTo(inputFile);

            if (!GroupLog.delete()) {
                success = false;
            }

        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }

        return success;
    }



}
