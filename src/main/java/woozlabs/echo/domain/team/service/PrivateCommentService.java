package woozlabs.echo.domain.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.team.dto.PrivateCommentCreateDto;
import woozlabs.echo.domain.team.entity.PrivateComment;
import woozlabs.echo.domain.team.repository.PrivateCommentRepository;

@Service
@RequiredArgsConstructor
public class PrivateCommentService {

    private final PrivateCommentRepository privateCommentRepository;

    public void CreateComment(PrivateCommentCreateDto privateCommentCreateDto) {
        PrivateComment comment = new PrivateComment();
        comment.setSharedEmailId(privateCommentCreateDto.getSharedEmailId());
        comment.setAuthorId(privateCommentCreateDto.getSharedEmailId());
        comment.setEncryptedContents(privateCommentCreateDto.getEncryptedContents());

        privateCommentRepository.save(comment);
    }
}
