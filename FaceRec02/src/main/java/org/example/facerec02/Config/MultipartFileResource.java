package org.example.facerec02.Config;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public class MultipartFileResource extends InputStreamResource {
    private final MultipartFile multipartFile;

    public MultipartFileResource(MultipartFile multipartFile) throws IOException {
        super(multipartFile.getInputStream());
        this.multipartFile = multipartFile;
    }

    @Override
    public String getFilename() {
        return multipartFile.getOriginalFilename();
    }

    @Override
    public long contentLength() throws IOException {
        return multipartFile.getSize();
    }
}
