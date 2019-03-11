package com.mihwapp.crazymusic.utils;



import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RestClient {

    private static final String TAG = RestClient.class.getSimpleName();

    private static final int TIMEOUT_CONNECTION = 15000;
    private static final int READ_TIMEOUT = 15000;

    public static final String LINE_END = "\r\n";
    public static final String TWO_HYPHENS = "--";
    public static final String BOUNDARY = "*****";
    public static final int MAX_BUFFER_SIZE = 4096;

    private ArrayList<KeyValueObject> params;
    private ArrayList<KeyValueObject> headers;
    private String mUrlApiEndpoint;
    private String response;
    private String message;
    private int responseCode;
    private InputStream inputStreamRes;
    private boolean isEncodeParams = true;
    private boolean isAppendParamsInGET = true;
    private boolean isUploadFile;

    private String uploadFilePath;
    private String nameFileParams;
    private String mimeType;


    public RestClient(String url) {
        this.mUrlApiEndpoint = url;
        this.params = new ArrayList<KeyValueObject>();
        this.headers = new ArrayList<KeyValueObject>();
    }
    public RestClient(String url, boolean isEncodeParams) {
        this.mUrlApiEndpoint = url;
        this.isEncodeParams = isEncodeParams;
        this.params = new ArrayList<KeyValueObject>();
        this.headers = new ArrayList<KeyValueObject>();
    }

    public void setIsUploadFile(boolean isUploadFile) {
        this.isUploadFile = isUploadFile;
        if(isUploadFile){
            headers.add(new KeyValueObject("Connection", "Keep-Alive"));
            headers.add(new KeyValueObject("ENCTYPE", "multipart/form-data"));
            headers.add(new KeyValueObject("Content-Type", "multipart/form-data;BOUNDARY=" + BOUNDARY));
        }
    }

    public void setUploadFilePath(String uploadFilePath, String nameParams, String mimeType) {
        this.uploadFilePath = uploadFilePath;
        this.nameFileParams=nameParams;
        this.mimeType=mimeType;
        if(isUploadFile){
            headers.add(new KeyValueObject(nameFileParams, uploadFilePath));
        }
    }

    public void addParams(String name, String value) {
        this.params.add(new KeyValueObject(name, value));
    }

    public void addHeader(String name, String value) {
        this.headers.add(new KeyValueObject(name, value));
    }

    public void setIsAppendParamsInGET(boolean isAppendParamsInGET) {
        this.isAppendParamsInGET = isAppendParamsInGET;
    }

    public String getmUrlApiEndpoint() {
        return mUrlApiEndpoint;
    }

    public void setmUrlApiEndpoint(String mUrlApiEndpoint) {
        this.mUrlApiEndpoint = mUrlApiEndpoint;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void excute(RequestMethod resMethod, boolean isGetInput) throws UnsupportedEncodingException {
        switch (resMethod) {
            case GET:
                String params = appendParams();
                String mUriEndpoint = mUrlApiEndpoint;
                if(isAppendParamsInGET){
                    if (!StringUtils.isEmpty(params)) {
                        mUriEndpoint = mUriEndpoint + params;
                    }
                    DBLog.d(TAG,"==========>mUriEndpoint="+mUriEndpoint);
                    executeRequest("GET", mUriEndpoint, null, isGetInput);
                }
                else{
                    executeRequest("GET", mUriEndpoint, params, isGetInput);
                }
                break;
            case POST:
                if(!isUploadFile){
                    String param1 = appendParams();
                    executeRequest("POST", mUrlApiEndpoint, param1, isGetInput);
                }
                else{
                    executeUploadRequest("POST", mUrlApiEndpoint, isGetInput);
                }
                break;
            default:
                break;
        }
    }

    private String appendParams() {
        if (params != null && params.size() > 0) {
            StringBuilder mStringBuilder = new StringBuilder();
            int size = params.size();
            for (int i = 0; i < size; i++) {
                KeyValueObject mKeyValueObject = params.get(i);
                mStringBuilder.append(mKeyValueObject.getKey());
                mStringBuilder.append("=");
                mStringBuilder.append(isEncodeParams ? StringUtils.urlEncodeString(mKeyValueObject.getValue()) : mKeyValueObject.getValue());
                if (i != size - 1) {
                    mStringBuilder.append("&");
                }
            }
            return mStringBuilder.toString();
        }
        return null;
    }

    private void executeRequest(String resMethod, String mUrlApiEndpoint, String params, boolean isGetInput) {
        try {
            InputStream inputStream=null;
            URL url = new URL(mUrlApiEndpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIMEOUT_CONNECTION);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setDoInput(true); // Allow Inputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod(resMethod);

            if (headers != null && headers.size() > 0) {
                for (KeyValueObject mKeyValueObject : headers) {
                    conn.addRequestProperty(mKeyValueObject.getKey(), mKeyValueObject.getValue());
                }
            }
            if (!StringUtils.isEmpty(params)) {
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(params.getBytes());
            }
            conn.connect();
            int serverResponseCode = conn.getResponseCode();
            DBLog.d(TAG, "executeRequest httcode= : " + serverResponseCode);
            inputStream = conn.getInputStream();
            if (serverResponseCode == HttpURLConnection.HTTP_OK) {
                if (!isGetInput) {
                    response = convertStreamToString(inputStream);
                    inputStream.close();
                }
                else {
                    inputStreamRes = inputStream;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executeUploadRequest(String resMethod, String mUrlApiEndpoint, boolean isGetInput){
        try{
            if(!StringUtils.isEmpty(uploadFilePath)){
                File mFile = new File(uploadFilePath);
                if(mFile.exists() && mFile.isFile()){
                    FileInputStream fileInputStream = new FileInputStream(mFile);
                    int bytesRead;
                    byte[] buffer;
                    URL url = new URL(mUrlApiEndpoint);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(TIMEOUT_CONNECTION);
                    conn.setReadTimeout(READ_TIMEOUT);
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow output
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod(resMethod);
                    if (headers != null && headers.size() > 0) {
                        for (KeyValueObject mKeyValueObject : headers) {
                            conn.setRequestProperty(mKeyValueObject.getKey(), mKeyValueObject.getValue());
                        }
                    }
                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                    if(params!=null && params.size()>0){
                        for(KeyValueObject mKeyValueObject:params){
                            dos.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
                            dos.writeBytes("Content-Disposition: form-data; name=\""+mKeyValueObject.getKey()+"\"" + LINE_END);
                            dos.writeBytes(LINE_END);
                            dos.writeBytes(mKeyValueObject.getValue());
                            dos.writeBytes(LINE_END);
                        }
                    }
                    dos.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
                    dos.writeBytes("Content-Disposition: form-data; name=\""+nameFileParams+"\"" + ";filename=\"" + uploadFilePath + "\"" + LINE_END);
                    if(!StringUtils.isEmpty(mimeType)){
                        String type = "Content-Type: "+mimeType+LINE_END;
                        dos.writeBytes(type);
                        dos.writeBytes(LINE_END);
                    }
                    dos.flush();

                    // create a buffer of  maximum size
                    buffer = new byte[MAX_BUFFER_SIZE];
                    while ((bytesRead = fileInputStream.read(buffer))!=-1) {
                        dos.write(buffer, 0, bytesRead);
                    }
                    dos.flush();
                    dos.writeBytes(LINE_END);
                    dos.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_END);

                    int serverResponseCode = conn.getResponseCode();
                    InputStream inputStream = conn.getInputStream();
                    if (serverResponseCode == HttpURLConnection.HTTP_OK) {
                        if (!isGetInput) {
                            response = convertStreamToString(inputStream);
                            inputStream.close();
                        }
                        else {
                            inputStreamRes = inputStream;
                        }
                    }
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                }
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public InputStream getInputStreamRes() {
        return inputStreamRes;
    }

    public void setInputStreamRes(InputStream inputStreamRes) {
        this.inputStreamRes = inputStreamRes;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                is.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public ArrayList<KeyValueObject> getHeaders() {
        return headers;
    }


    public ArrayList<KeyValueObject> getParams() {
        return params;
    }


    public enum RequestMethod {
        GET, POST
    }

    public class KeyValueObject {
        public String key;
        public String value;

        public KeyValueObject(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
