package com.example.taskifygo.remind;

import android.nfc.Tag;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class XlAccess {

    private static String Tag = "XlAccess";
    private ExecutorService executor;

    public XlAccess() {
        executor = Executors.newSingleThreadExecutor(); // Single thread executor for background tasks
    }

    public void sendDataToServer(final String urlString,final String uniqueIdExist, final String userName, final String taskDetails,
                                 final String timeTime, final String frequency) {

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // Perform network operation on background thread
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);

                    String timeStamp = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
                    System.out.println("Generated Unique ID: " + timeStamp);
                    String uniqueIdNew = timeStamp+userName;
                    // Prepare POST data
                    String postData = "uniqueIdNew="+uniqueIdNew+
                            "&uniqueIdExist="+uniqueIdExist+
                            "&userName=" + userName +
                            "&taskDetails=" + taskDetails +
                            "&timeTime=" + timeTime +
                            "&frequency=" + frequency+
                            "&status=Y";

                    System.out.println("GUNAGUNA:"+postData);
                    OutputStream os = connection.getOutputStream();
                    os.write(postData.getBytes());
                    os.flush();

                    // Get the response code
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        System.out.print("Success: Handle success response"+connection.getResponseMessage());// Success: Handle success response
                    } else {
                        System.out.print("Failure: Handle failure response");// Failure: Handle failure response
                    }
                } catch (Exception e) {
                    Log.e(Tag,e.getMessage());
                }
            }
        });
    }


    public void getDataFromServer(final String urlString, final DataCallback callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // Perform GET request on background thread
                    URL url = new URL(urlString+ "?method=doGetRow");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Pass the response to the callback
                    if (callback != null) {
                        callback.onSuccess(response.toString());
                    }

                } catch (Exception e) {
                    Log.e(Tag,e.getMessage());
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }
            }
        });
    }


    public void deleteDataFromServer(final String urlString,final String user, final int taskId, final DataCallback callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // Append the taskId to the URL for deletion
                    URL url = new URL(urlString + "?method=doDelete&sno="+taskId+"&userName="+user);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Get the response code
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Success: Notify success via callback
                        if (callback != null) {
                            callback.onSuccess("Task deleted successfully");
                        }
                    } else {
                        // Failure: Notify failure via callback
                        if (callback != null) {
                            callback.onFailure(new Exception("Failed to delete task. Response code: " + responseCode));
                        }
                    }
                } catch (Exception e) {
                    Log.e(Tag,e.getMessage());
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }
            }
        });
    }



    public void changeStatusToServer(final String urlString,final String uniqueId,final String user, final String taskDetails,String status) {

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // Append the taskId to the URL for deletion
                    URL url = new URL(urlString + "?method=doStatus&userName="+user+
                            "&uniqueId="+uniqueId+
                            "&taskDetails="+taskDetails+
                            "&status="+status);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Get the response code
                    System.out.println("Check status"+connection.getResponseMessage());
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        // Print the response
                        System.out.println("Response: " + response.toString());
                    }
                } catch (Exception e) {
                    Log.e(Tag,e.getMessage());
                }
            }
        });
    }


    public void getUserData(final String urlString, final DataCallback callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // Perform GET request on background thread
                    URL url = new URL(urlString+ "?method=doGetUserData");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Pass the response to the callback
                    if (callback != null) {
                        callback.onSuccess(response.toString());
                    }

                } catch (Exception e) {
                    Log.e(Tag,e.getMessage());
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }
            }
        });
    }

    public void setUserData(final String urlString,final String user, final String password,final String email, final String userType) {

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // Append the taskId to the URL for deletion
                    URL url = new URL(urlString + "?method=doPostUserData&userName="+user+
                            "&password="+password+
                            "&email="+email+
                            "&userType="+userType);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Get the response code
                    System.out.println("Check status"+connection.getResponseMessage());
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        // Print the response
                        System.out.println("Response: " + response.toString());
                    }
                } catch (Exception e) {
                    Log.e(Tag,e.getMessage());
                }
            }
        });
    }

    public void validCredentials(final String urlString,final String user, final String password, final DataCallback Callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // Perform GET request on background thread
                    URL url = new URL(urlString+ "?method=doValidCredentials&user="+user+
                            "&password="+password);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Pass the response to the callback
                    if (Callback != null) {
                        Callback.onSuccess(response.toString());
                    }

                } catch (Exception e) {
                    Log.e(Tag,e.getMessage());
                    if (Callback != null) {
                        Callback.onFailure(e);
                    }
                }
            }
        });
    }


    public void updateReminderTime(final String urlString,final String uniqueId,final String time) {

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // Append the taskId to the URL for deletion
                    URL url = new URL(urlString + "?method=doUpdateTime&uniqueId="+uniqueId+
                            "&time="+time);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Get the response code
                    System.out.println("Check status"+connection.getResponseMessage());
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        // Print the response
                        System.out.println("Response-Date Update: " + response.toString());
                    }
                } catch (Exception e) {
                    Log.e(Tag,e.getMessage());
                }
            }
        });
    }

    //NOtes

    public void addOrUpdateNotes(final String urlString,final String user, final String notes,final String uniqueId,final String title, final DataCallback callback) {

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    // Append the taskId to the URL for deletion
                    /*URL url = new URL(urlString + "?method=doAddOrUpdateNotes&userName="+user+
                            "&notes="+notes+
                            "&title="+title+
                            "&uniqueId="+uniqueId);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");*/

                    String postData = "userName="+user+
                            "&notes="+notes+
                            "&title=" + title +
                            "&uniqueId=" + uniqueId;

                    System.out.println("notes input:"+postData);
                    OutputStream os = connection.getOutputStream();
                    os.write(postData.getBytes());
                    os.flush();

                    // Get the response code
                    System.out.println("notes add status"+connection.getResponseMessage());
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        // Print the response
                        System.out.println("Response: " + response.toString());
                        if (callback != null) {
                            callback.onSuccess(response.toString());
                        }
                    }catch (Exception e){Log.e(Tag,"Error in save note:"+e.getMessage());}
                } catch (Exception e) {
                    Log.e(Tag,"Error:"+e.getMessage());
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }
            }
        });
    }


    public void getNotes(final String urlString, final DataCallback callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // Perform GET request on background thread
                    URL url = new URL(urlString+ "?method=doGetNotes");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Pass the response to the callback
                    if (callback != null) {
                        callback.onSuccess(response.toString());
                    }

                } catch (Exception e) {
                    Log.e(Tag,e.getMessage());
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }
            }
        });
    }

    public void deletenotes(final String urlString,final String user, final String uniqueId,final DataCallback callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // Append the taskId to the URL for deletion
                    URL url = new URL(urlString + "?method=doDeleteNotes&uniqueId="+uniqueId+"&userName="+user);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Get the response code
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Success: Notify success via callback
                        if (callback != null) {
                            callback.onSuccess("Task deleted successfully");
                        }
                    } else {
                        // Failure: Notify failure via callback
                        if (callback != null) {
                            callback.onFailure(new Exception("Failed to delete task. Response code: " + responseCode));
                        }
                    }
                } catch (Exception e) {
                    Log.e(Tag,e.getMessage());
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }
            }
        });
    }

    public interface DataCallback {
        void onSuccess(String response);
        void onFailure(Exception e);
    }
}
