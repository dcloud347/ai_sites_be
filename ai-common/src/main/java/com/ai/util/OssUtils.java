package com.ai.util;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;

public class OssUtils {
    String yourSasToken = "sv=2022-11-02&ss=bfqt&srt=sco&sp=rwdlacupiytfx&se=2025-05-30T18:33:51Z&st=2024-05-30T10:33:51Z&spr=https&sig=O36ij%2Bc6LxCAOzcg%2BmYZWRzIcJPQwK%2FMZB8ROzKhFd4%3D";

    public String uploadFile(String file_path,String remote_file_name) throws BlobStorageException {

        /* Create a new BlobServiceClient with a SAS Token */
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .endpoint("https://aisiteadmin.blob.core.windows.net")
                .sasToken(yourSasToken)
                .buildClient();

        /* Create a new container client */
        BlobContainerClient containerClient;
        try {
            containerClient = blobServiceClient.createBlobContainer("ai-sites-avatar");
        } catch (BlobStorageException ex) {
            // The container may already exist, so don't throw an error
            if (!ex.getErrorCode().equals(BlobErrorCode.CONTAINER_ALREADY_EXISTS)) {
                throw ex;
            }else{
                containerClient = blobServiceClient.getBlobContainerClient("ai-sites-avatar");
            }
        }

        /* Upload the file to the container */
        BlobClient blobClient = containerClient.getBlobClient(remote_file_name);
        blobClient.uploadFromFile(file_path);
        return blobClient.getBlobUrl();
    }
    public static void main(String[] args){
        OssUtils ossUtils = new OssUtils();
    }


}
