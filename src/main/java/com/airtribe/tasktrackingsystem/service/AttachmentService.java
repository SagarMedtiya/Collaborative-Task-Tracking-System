package com.airtribe.tasktrackingsystem.service;

import com.airtribe.tasktrackingsystem.dto.response.AttachmentResponse;
import com.airtribe.tasktrackingsystem.exception.ResourceNotFoundException;
import com.airtribe.tasktrackingsystem.model.Attachments;
import com.airtribe.tasktrackingsystem.model.Task;
import com.airtribe.tasktrackingsystem.model.Users;
import com.airtribe.tasktrackingsystem.repository.AttachmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final TaskService taskService;
    private final UserService userService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Transactional
    public AttachmentResponse uploadAttachment(Long taskId, MultipartFile file, String username) {
        Task task = taskService.getEntityById(taskId);
        Users user = userService.getEntityByUsername(username);

        try{
            //Create upload dir if not exists
            Path uploadPath = Paths.get(uploadDir);
            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }
            //Generate unique filename
            String originalFileName = file.getOriginalFilename();
            String fileName = UUID.randomUUID().toString() + "_" + originalFileName;
            //save file
            Path destination = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), destination);
            //create attachment entity
            Attachments attachment = Attachments.builder()
                    .fileName(originalFileName)
                    .filePath(destination.toString())
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .task(task)
                    .uploadedBy(user)
                    .build();
            attachment = attachmentRepository.save(attachment);
            return AttachmentResponse.from(attachment);
        }catch(IOException e){
            throw new RuntimeException("Failed to upload file: "+ e.getMessage());
        }
    }

    public AttachmentResponse getAttachment(Long id){
        Attachments attachment = attachmentRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Attachment not found"));
        return AttachmentResponse.from(attachment);
    }
    @Transactional
    public void deleteAttachment(Long id, String username){
        Attachments attachment = attachmentRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Attachment not found"));
        Users user = userService.getEntityByUsername(username);
        if(!attachment.getUploadedBy().getId().equals(user.getId())){
            throw new RuntimeException("You can only delete your own attachments");
        }
        //Delete physical file
        try{
            Path filePath = Paths.get(attachment.getFilePath());
            Files.deleteIfExists(filePath);
        }catch(Exception e){
            System.err.println("Failed to delete physical file: "+ e.getMessage());
        }
        attachmentRepository.delete(attachment);
    }

}













