package com.example.analyzer.controller;

import com.example.analyzer.model.Analysis;
import com.example.analyzer.repo.AnalysisRepository;
import com.example.analyzer.service.SeverityService;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class AnalysisController {

  private final AnalysisRepository repo;
  private final SeverityService severity;

  public AnalysisController(AnalysisRepository repo, SeverityService severity) {
    this.repo = repo;
    this.severity = severity;
  }

  /** Create or save one analysis from frontend JSON */
  @PostMapping("/analyze")
  public ResponseEntity<Analysis> create(@RequestBody Map<String, Object> body) {
    Analysis a = new Analysis();
    a.setUsername(Objects.toString(body.getOrDefault("username",""), ""));
    a.setPlatform(Objects.toString(body.getOrDefault("platform",""), ""));
    a.setScore(((Number) body.getOrDefault("score", 0)).intValue());

    // flexible keys to match your front-end
    @SuppressWarnings("unchecked")
    List<String> keywords = (List<String>) body.getOrDefault("detectedKeywords",
                            body.getOrDefault("keywords", new ArrayList<String>()));
    @SuppressWarnings("unchecked")
    List<String> types = (List<String>) body.getOrDefault("detectedDrugTypes",
                         body.getOrDefault("detectedTypes", new ArrayList<String>()));

    a.setDetectedKeywords(new ArrayList<>(keywords));
    a.setDetectedDrugTypes(new ArrayList<>(types));

    // compute status on server too (demo)
    a.setStatus(severity.computeStatus(a.getScore(), a.getDetectedDrugTypes()));

    Analysis saved = repo.save(a);
    return ResponseEntity.ok(saved);
  }

  /** Full list (newest first) */
  @GetMapping("/analyses")
  public List<Analysis> listAll() {
    return repo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
  }

  /** Pageable list */
  @GetMapping("/analyses/page")
  public Page<Analysis> listPage(@RequestParam(defaultValue="0") int page,
                                 @RequestParam(defaultValue="20") int size) {
    return repo.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
  }

  /** Simple search by platform and/or minimum score */
  @GetMapping("/analyses/search")
  public List<Analysis> search(@RequestParam(required=false) String platform,
                               @RequestParam(required=false) Integer minScore) {
    return repo.findAll().stream()
      .filter(a -> platform == null || platform.equalsIgnoreCase(a.getPlatform()))
      .filter(a -> minScore == null || a.getScore() >= minScore)
      .sorted(Comparator.comparing(Analysis::getCreatedAt).reversed())
      .toList();
  }

  /** Suspend an analysis row (demo admin action) */
  @PostMapping("/analyses/{id}/suspend")
  public ResponseEntity<Analysis> suspend(@PathVariable Long id) {
    Analysis a = repo.findById(id).orElseThrow();
    a.setStatus("Suspended");
    return ResponseEntity.ok(repo.save(a));
  }

  /** Delete a row */
  @DeleteMapping("/analyses/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    repo.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
