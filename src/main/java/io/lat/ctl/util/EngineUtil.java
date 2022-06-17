package io.lat.ctl.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.lat.ctl.exception.LatException;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EngineUtil {
    public static void modifyEngine(String server_id, String version, String serverType){



        String installRootPath = FileUtil.getConcatPath(EnvUtil.getLatHome(), "instances", serverType);
        String envPath = FileUtil.getConcatPath(installRootPath, server_id, "env.sh");
        String currentVersion = FileUtil.getShellVariableString(envPath, "ENGN_VERSION");
        currentVersion = currentVersion.substring(13);

        Process proc;

        List<String> args = new ArrayList<String>();
        System.out.println(installRootPath+"/"+server_id+"/stop.sh");
        args.add(installRootPath+"/"+server_id+"/stop.sh");
        try {
            proc = Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
        }
        catch (IOException e) {
            System.out.println("Failed to stop the instance.");
            return;
        }

        System.out.println("VERSION: "+version);
        System.out.println("CURRENT VERSION: "+currentVersion);

        String[] splitVersion = version.split("\\.");
        String[] splitCurrentVersion = currentVersion.split("\\.");

        for(int i=0; i<2; i++){
            if(!splitVersion[i].equals(splitCurrentVersion[i])){
                System.out.println("It is not allowed to modify the major version");
                return;
            }
        }

        Collection<File> runtimes = getInstalledEngines(serverType);

        Iterator<File> it = runtimes.iterator();
        boolean isInstalled = false;
        while(it.hasNext()){
            if(it.next().getName().substring(7).equals(version)) {
                isInstalled=true;
                break;
            }
        }
        if(isInstalled) {
            FileUtil.setShellVariable(envPath, "ENGN_VERSION", version);
            return;
        }

        System.out.println(version+" is not installed. Install the version first.");
        return;
    }

    public static Collection<File> getInstalledEngines(String serverType){
        String runtimePath = FileUtil.getConcatPath(EnvUtil.getLatHome(), "engines", serverType);

        //System.out.println(runtimePath);

        Collection<File> runtimes = CustomFileUtils.listDirectories(new File(runtimePath), new WildcardFileFilter("*"), TrueFileFilter.INSTANCE);
        /*
        for(File file:runtimes){

            System.out.println(file.getName());
        }

         */
        return runtimes;
    }


    public static void listEngines(String serverType) throws URISyntaxException, ExecutionException, InterruptedException {
        List<String> availableList = getEnginesFromGithub(serverType);
        Collection<File> installedList = getInstalledEngines(serverType);

        for(String version : availableList){
            version = version.substring(0, version.length()-7);
            System.out.print(version.substring(serverType.length()+1));


            Iterator<File> it = installedList.iterator();
            while(it.hasNext()){
                File file = it.next();
                if(file.getName().equals(version)){
                    System.out.print(" *");
                    break;
                }
            }
            System.out.println();
        }
    }
    public static List<String> getEnginesFromGithub(String serverType) throws URISyntaxException, ExecutionException, InterruptedException {
        String address = "https://api.github.com/repos/ATLENA/lat-"+serverType+"-runtimes/git/trees/main";

        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        String result = client.sendAsync(
                        HttpRequest.newBuilder(
                                new URI(address)).GET().build(),  //GET방식 요청
                        HttpResponse.BodyHandlers.ofString()  //응답은 문자형태
                ).thenApply(HttpResponse::body)  //thenApply메소드로 응답body값만 받기
                .get();  //get메소드로 body값의 문자를 확인

        Gson gson = new Gson();
        JsonObject jo = gson.fromJson(result, JsonObject.class);

 List<String> re = new ArrayList<String>();

        JsonArray ja = jo.get("tree").getAsJsonArray();

        for(int i=0; i<ja.size(); i++){
            String file = ja.get(i).getAsJsonObject().get("path").getAsString();
            if(file.endsWith(".tar.gz")){
                re.add(file);
                //System.out.println(file);
            }

        }

        return re;
    }

    public static void downloadEngine(String version, String serverType) throws Exception {

        String FILE_NAME = serverType+"-"+version+".tar.gz";
        String FILE_URL = "https://github.com/ATLENA/lat-"+serverType+"-runtimes/raw/main/"+FILE_NAME;
        String FILE_PATH = FileUtil.getConcatPath(EnvUtil.getLatHome(), "engines", serverType);


        System.out.println("Downloading file from "+FILE_URL+".....");


        ReadableByteChannel rbc = Channels.newChannel(new URL(FILE_URL).openStream());
        FileOutputStream fos = new FileOutputStream(FILE_NAME);

        fos.getChannel().transferFrom(rbc, 0,  Long.MAX_VALUE);
        fos.close();

        System.out.println("Downloading completed.");

        File compressedFile = new File(FILE_NAME);
        File decompressedFile = new File(FILE_NAME.substring(0,FILE_NAME.length()-3));
        decompressedFile = decompress(compressedFile, decompressedFile);

        //System.out.println("tar = "+decompressedFile.getName());
        System.out.println("Path to install = "+FILE_PATH);

        File[] files = unarchive(decompressedFile, new File(FileUtil.getConcatPath(FILE_PATH, serverType+"-"+version)));

        CustomFileUtils.deleteQuietly(compressedFile);
        CustomFileUtils.deleteQuietly(decompressedFile);
    }

    public static File decompress(File compressedFile, File decompressedFile) {
        InputStream inputStream = null;
        CompressorInputStream compressorInputStream = null;
        FileOutputStream outputStream = null;

        try {
            inputStream = new FileInputStream(compressedFile);
            compressorInputStream = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, inputStream);
            outputStream = new FileOutputStream(decompressedFile);
            IOUtils.copy(compressorInputStream, outputStream);

        } catch (CompressorException e) {
            throw new IllegalStateException("Fail to compress a file.", e);
        } catch (IOException e) {
            throw new LatException("An I/O error has occurred : " + e);
        } finally {
            FileUtil.close(compressorInputStream);
            FileUtil.close(inputStream);
            FileUtil.close(outputStream);
        }
        return decompressedFile;
    }

    /**
     *
     * unarchive를 수행한다.<br>
     *
     * @param compressedFile archive 파일명
     * @param destinationDirectory unarchive 대상이 되는 디렉토리 경로
     * @return unarchive된 파일 경로 리스트
     *
     */
    public static File[] unarchive(File compressedFile, File destinationDirectory) {
        File[] resultList = null;

        try {
            final InputStream inputStream = new FileInputStream(compressedFile);
            ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();


            ArchiveInputStream archiveInputStream = archiveStreamFactory.createArchiveInputStream(ArchiveStreamFactory.TAR, inputStream);

            byte[] buffer = new byte[65536];

            TarArchiveEntry entry = (TarArchiveEntry)archiveInputStream.getNextEntry();

            while (entry != null) {
                String name = entry.getName();

                name = name.replace('\\', '/');

                File destinationFile = new File(destinationDirectory, name);
                // extract 버그 수정
                if (!name.endsWith("/")) {
                    File parentFolder = destinationFile.getParentFile();
                    if (!parentFolder.exists()) {
                        parentFolder.mkdirs();
                    }

                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(destinationFile);
                        int length = archiveInputStream.read(buffer);
                        while (length != -1) {
                            if (length > 0) {
                                outputStream.write(buffer, 0, length);
                            }
                            length = archiveInputStream.read(buffer);
                        }
                    } finally {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                }
                else{
                    destinationFile.mkdirs();
                }
                entry = (TarArchiveEntry)archiveInputStream.getNextEntry();
            }
            resultList = destinationDirectory.listFiles();
            FileUtil.close(inputStream);

        } catch (ArchiveException e) {
            throw new IllegalStateException("Fail to unarchive the files.", e);
        } catch (IOException e) {
            throw new LatException("An I/O error has occurred : " + e);
        }
        return resultList;
    }
}
