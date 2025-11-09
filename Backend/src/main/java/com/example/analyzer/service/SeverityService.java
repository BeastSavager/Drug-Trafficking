package com.example.analyzer.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SeverityService {

  // simple demo rule set
  public String computeStatus(int score, List<String> types) {
    if (score >= 75) return "Suspended";
    if (score >= 50) return "Under Review";
    return "Active";
  }
}
