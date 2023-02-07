package br.com.luislaurindo.azure;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

public class AzureStorage {

    public static CloudBlobClient connect(String key) throws URISyntaxException, InvalidKeyException {
        CloudStorageAccount cloudStorageAccount = CloudStorageAccount.parse(key);
        return cloudStorageAccount.createCloudBlobClient();
    }

    public static CloudBlobContainer getCloudBlobContainer(CloudBlobClient cloudBlobClient, String containerName) throws URISyntaxException, StorageException {
        return cloudBlobClient.getContainerReference(containerName);
    }

    public static String upload(CloudBlobContainer cloudBlobContainer, InputStream inputStream, String fileName) throws URISyntaxException, StorageException, IOException {
        CloudBlockBlob cloudBlockBlob = cloudBlobContainer.getBlockBlobReference(fileName);
        cloudBlockBlob.upload(inputStream, -1);



        return cloudBlockBlob.getStorageUri().getPrimaryUri().toString();
    }
}