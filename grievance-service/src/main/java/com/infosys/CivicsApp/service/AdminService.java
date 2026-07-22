package com.infosys.CivicsApp.service;

import com.infosys.CivicsApp.client.CitizenClient;
import com.infosys.CivicsApp.client.DepartmentClient;
import com.infosys.CivicsApp.client.OfficerClient;
import com.infosys.CivicsApp.dto.CitizenDto;
import com.infosys.CivicsApp.dto.DepartmentDto;
import com.infosys.CivicsApp.dto.OfficerDto;
import com.infosys.CivicsApp.entity.Grievance;
import com.infosys.CivicsApp.repository.GrievanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private CitizenClient citizenClient;

    @Autowired
    private GrievanceRepository grievanceRepository;

    @Autowired
    private OfficerClient officerClient;

    @Autowired
    private DepartmentClient departmentClient;

    public Map<String, Object> getDashboardStats() {
        List<Grievance> grievances = grievanceRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        long totalCitizens = 0;
        try {
            totalCitizens = citizenClient.getCitizenCount();
        } catch (Exception e) {
            System.err.println("Could not load citizen count: " + e.getMessage());
        }

        long totalComplaints = grievances.size();

        long resolvedComplaints = grievances.stream()
                .filter(g -> "RESOLVED".equalsIgnoreCase(g.getStatus()) || "CLOSED".equalsIgnoreCase(g.getStatus()))
                .count();

        long pendingComplaints = totalComplaints - resolvedComplaints;

        long overdueComplaints = grievances.stream()
                .filter(g -> !"RESOLVED".equalsIgnoreCase(g.getStatus()) && !"CLOSED".equalsIgnoreCase(g.getStatus()))
                .filter(g -> g.getSlaDeadline() != null && g.getSlaDeadline().isBefore(now))
                .count();

        long escalatedComplaints = grievanceRepository.countByEscalated(true);

        long totalDepartments = 0;
        try {
            totalDepartments = departmentClient.getDepartmentCount();
        } catch (Exception e) {
            System.err.println("Could not load department count: " + e.getMessage());
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCitizens", totalCitizens);
        stats.put("totalComplaints", totalComplaints);
        stats.put("pendingComplaints", pendingComplaints);
        stats.put("resolvedComplaints", resolvedComplaints);
        stats.put("overdueComplaints", overdueComplaints);
        stats.put("escalatedComplaints", escalatedComplaints);
        stats.put("totalDepartments", totalDepartments);

        return stats;
    }

    public Map<String, Object> getPerformanceReport() {
        List<Grievance> grievances = grievanceRepository.findAll();
        long totalComplaints = grievances.size();

        long resolvedCount = grievances.stream()
                .filter(g -> "RESOLVED".equalsIgnoreCase(g.getStatus()) || "CLOSED".equalsIgnoreCase(g.getStatus()))
                .count();

        double resolutionRate = totalComplaints == 0 ? 0.0 : (double) resolvedCount * 100.0 / totalComplaints;

        // Daily trend (last 7 days)
        Map<String, Long> dailyTrends = new LinkedHashMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 6; i >= 0; i--) {
            String dateKey = LocalDateTime.now().minusDays(i).format(dateFormatter);
            dailyTrends.put(dateKey, 0L);
        }
        
        grievances.stream()
                .filter(g -> g.getCreatedDate() != null)
                .forEach(g -> {
                    String dateKey = g.getCreatedDate().format(dateFormatter);
                    if (dailyTrends.containsKey(dateKey)) {
                        dailyTrends.put(dateKey, dailyTrends.get(dateKey) + 1);
                    }
                });

        // Department-wise Performance metrics
        List<Map<String, Object>> deptPerformance = new ArrayList<>();
        try {
            List<DepartmentDto> departments = departmentClient.getAllDepartments();
            if (departments != null) {
                deptPerformance = departments.stream().map(dept -> {
                    List<Grievance> deptGrievances = grievances.stream()
                            .filter(g -> g.getDepartmentId() != null && g.getDepartmentId().equals(dept.getId()))
                            .collect(Collectors.toList());

                    long deptTotal = deptGrievances.size();
                    long deptResolved = deptGrievances.stream()
                            .filter(g -> "RESOLVED".equalsIgnoreCase(g.getStatus()) || "CLOSED".equalsIgnoreCase(g.getStatus()))
                            .count();

                    double deptResolutionRate = deptTotal == 0 ? 0.0 : (double) deptResolved * 100.0 / deptTotal;

                    Map<String, Object> deptMap = new HashMap<>();
                    deptMap.put("departmentId", dept.getId());
                    deptMap.put("departmentName", dept.getName());
                    deptMap.put("totalComplaints", deptTotal);
                    deptMap.put("resolvedComplaints", deptResolved);
                    deptMap.put("resolutionRate", Math.round(deptResolutionRate * 10.0) / 10.0);
                    return deptMap;
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Could not compile department performance report: " + e.getMessage());
        }

        Map<String, Object> report = new HashMap<>();
        report.put("totalComplaints", totalComplaints);
        report.put("resolvedComplaints", resolvedCount);
        report.put("resolutionRate", Math.round(resolutionRate * 10.0) / 10.0);
        report.put("dailyComplaints", dailyTrends);
        report.put("departmentPerformance", deptPerformance);

        return report;
    }

    public Map<String, Object> getAllUsers() {
        Map<String, Object> users = new HashMap<>();
        
        List<Map<String, Object>> citizenList = new ArrayList<>();
        try {
            List<CitizenDto> citizens = citizenClient.getAllCitizens();
            if (citizens != null) {
                citizenList = citizens.stream().map(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getId());
                    m.put("name", c.getName());
                    m.put("email", c.getEmail());
                    m.put("phone", c.getPhone());
                    m.put("role", c.getRole());
                    m.put("ward", c.getWard());
                    m.put("city", c.getCity());
                    return m;
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Could not load citizens directory: " + e.getMessage());
        }

        List<Map<String, Object>> officerList = new ArrayList<>();
        try {
            List<OfficerDto> officers = officerClient.getAllOfficers();
            if (officers != null) {
                officerList = officers.stream().map(o -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", o.getId());
                    m.put("name", o.getName());
                    m.put("email", o.getEmail());
                    m.put("role", o.getRole());
                    m.put("departmentName", o.getDepartment() != null ? o.getDepartment().getName() : "Admin");
                    return m;
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Could not load officers directory: " + e.getMessage());
        }

        users.put("citizens", citizenList);
        users.put("officers", officerList);
        return users;
    }
}
