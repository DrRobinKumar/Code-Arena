package com.codearena.service;

import com.codearena.dto.request.RunCodeRequest;
import com.codearena.dto.request.SubmitCodeRequest;
import com.codearena.dto.response.PageResponse;
import com.codearena.dto.response.RunCodeResponse;
import com.codearena.dto.response.SubmissionDetailResponse;
import com.codearena.dto.response.SubmissionResponse;
import com.codearena.entity.Language;
import com.codearena.entity.Verdict;
import org.springframework.data.domain.Pageable;

public interface SubmissionService {

    /** Ad-hoc execution against custom input. Never persisted. */
    RunCodeResponse runCode(RunCodeRequest request);

    /** Grades code against a problem's full test-case set and persists the result. */
    SubmissionDetailResponse submitCode(SubmitCodeRequest request, String username);

    /** Owner-only lookup — a submission belonging to someone else is reported as not found. */
    SubmissionDetailResponse getOwnSubmission(Long id, String username);

    /** No ownership check — admin only, enforced at the controller. */
    SubmissionDetailResponse getSubmissionForAdmin(Long id);

    PageResponse<SubmissionResponse> getMySubmissions(String username, Long problemId, Verdict verdict,
                                                       Language language, Pageable pageable);

    PageResponse<SubmissionResponse> getAllSubmissions(Long userId, Long problemId, Verdict verdict,
                                                        Language language, Pageable pageable);
}
