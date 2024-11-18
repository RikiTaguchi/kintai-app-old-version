package com.management.task.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.management.task.model.Manager;
import com.management.task.model.Salary;
import com.management.task.model.User;
import com.management.task.model.Work;
import com.management.task.model.WorkTemplate;
import com.management.task.service.ManagerService;
import com.management.task.service.SalaryService;
import com.management.task.service.UserService;
import com.management.task.service.WorkService;
import com.management.task.service.WorkTemplateService;
import com.management.task.util.DateSet;

@Controller
public class HomeController {
    
    private final WorkService workService;
    private final UserService userService;
    private final ManagerService managerService;
    private final SalaryService salaryService;
    private final WorkTemplateService workTemplateService;
    
    @Autowired
    public HomeController(WorkService workService, UserService userService, ManagerService managerService, SalaryService salaryService, WorkTemplateService workTemplateService) {
        this.workService = workService;
        this.userService = userService;
        this.managerService = managerService;
        this.salaryService = salaryService;
        this.workTemplateService = workTemplateService;
    }
    
    // 講師ホーム画面
    @GetMapping("/index")
    String index(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            Date dateFrom = DateSet.getDatePeriod(year, month)[0];
            Date dateTo = DateSet.getDatePeriod(year, month)[1];
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            String yearNext = DateSet.getDateNext(year, month)[0];
            String monthNext = DateSet.getDateNext(year, month)[1];
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(user.getClassAreaId());
            int sumSalary[] = new int[16];
            double setDouble[] = new double[15];
            double resultDouble[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
            List<Work> workList = workService.findByUserId(UUID.fromString(userId), dateFrom, dateTo);
            for (Work work : workList) {
                setDouble = workService.calcSumSalary(work, salaryService.getByDate(UUID.fromString(userId), work.getDate()));
                for (int i = 0; i < 15; i++) {
                    resultDouble[i]+= setDouble[i]; 
                }
            }
            for (int i = 0; i < 15; i++) {
                sumSalary[i] = (int)Math.ceil(resultDouble[i]);
                if (i != 0 && i != 2 && i != 3 && i != 5 && i != 7 && i != 11 && i != 13) {
                    sumSalary[15] += sumSalary[i];
                }
            }
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("workList", workList);
            model.addAttribute("sumSalary", sumSalary);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("yearBefore", yearBefore);
            model.addAttribute("monthBefore", monthBefore);
            model.addAttribute("yearNext", yearNext);
            model.addAttribute("monthNext", monthNext);
            redirectAttributes.addAttribute("user", userId);
            return "index";
        } catch (Exception e) {
            System.out.println("Error happened in index.html");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // 社員ホーム画面
    @GetMapping("/indexManager")
    String indexManager(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId) {
        try {
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            Manager manager = managerService.getById(UUID.fromString(managerId));
            List<User> userList = userService.findByClassAreaId(UUID.fromString(managerId));
            model.addAttribute("manager", manager);
            model.addAttribute("userList", userList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("manager", managerId);
            return "indexManager";
        } catch (Exception e) {
            System.out.println("Error happened in indexManager.html");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // 給与詳細（講師用）
    @GetMapping("/detail")
    String detail(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(user.getClassAreaId());
            Date dateFrom = DateSet.getDatePeriod(year, month)[0];
            Date dateTo = DateSet.getDatePeriod(year, month)[1];
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            String yearNext = DateSet.getDateNext(year, month)[0];
            String monthNext = DateSet.getDateNext(year, month)[1];
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            Map<UUID, Salary> salaryMap = new HashMap<>();
            int sumSalaryPre[] = new int[17];
            int sumSalary[] = new int[17];
            double setDouble[] = new double[16];
            double resultDouble[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
            List<Work> workList = workService.findByUserId(UUID.fromString(userId), dateFrom, dateTo);
            try {
                sumSalary = workService.calcSumSalary(UUID.fromString(userId), dateFrom, dateTo, salary.getClassSalary(), salary.getOfficeSalary(), salary.getSupportSalary());
                for (Work work : workList) {
                    setDouble = workService.calcSumSalary(work, salaryService.getByDate(UUID.fromString(userId), work.getDate()));
                    salaryMap.put(work.getId(), salaryService.getByDate(UUID.fromString(userId), work.getDate()));
                    for (int i = 0; i < 16; i++) {
                        resultDouble[i]+= setDouble[i];
                    }
                }
                for (int i = 0; i < 16; i++) {
                    sumSalaryPre[i] = (int)Math.ceil(resultDouble[i]);
                    if (i != 0 && i != 2 && i != 3 && i != 5 && i != 7 && i != 10 && i != 12 && i != 14) {
                        sumSalaryPre[16] += sumSalaryPre[i];
                    }
                }
                sumSalary[16] += sumSalaryPre[15] - sumSalary[15] + sumSalaryPre[13] - sumSalary[13] + sumSalaryPre[9] - sumSalary[9] + sumSalaryPre[8] - sumSalary[8] + sumSalaryPre[6] - sumSalary[6] + sumSalaryPre[4] - sumSalary[4];
                sumSalary[15] = sumSalaryPre[15];
                sumSalary[13] = sumSalaryPre[13];
                sumSalary[9] = sumSalaryPre[9];
                sumSalary[8] = sumSalaryPre[8];
                sumSalary[6] = sumSalaryPre[6];
                sumSalary[4] = sumSalaryPre[4];
                sumSalary[10] = sumSalaryPre[16] - sumSalary[16];
                sumSalary[16] += sumSalary[10];
            } catch (Exception e) {
                for (int i = 0; i < 17; i++) {
                    sumSalary[i] = 0;
                    sumSalaryPre[i]= 0;
                }
            }
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salary", salary);
            model.addAttribute("sumSalary", sumSalary);
            model.addAttribute("salaryMap", salaryMap);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("yearBefore", yearBefore);
            model.addAttribute("monthBefore", monthBefore);
            model.addAttribute("yearNext", yearNext);
            model.addAttribute("monthNext", monthNext);
            redirectAttributes.addAttribute("user", userId);
            return "detail";
        } catch (Exception e) {
            System.out.println("Error happened in detail.html");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // 勤務詳細（講師用）
    @GetMapping("/detailWork")
    String detailWork(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("detail") String detailId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(user.getClassAreaId());
            Work work = workService.findWorkById(UUID.fromString(detailId));
            Salary salary = salaryService.getByDate(UUID.fromString(userId), work.getDate());
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salary", salary);
            model.addAttribute("work", work);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "detailWork";
        } catch (Exception e) {
            System.out.println("Error happened in detailWork.html");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // 給与詳細（社員用）
    @GetMapping("/detailUser")
    String detailUser(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @Param("year") String year, @Param("month") String month) {
        try {
            Date dateFrom = DateSet.getDatePeriod(year, month)[0];
            Date dateTo = DateSet.getDatePeriod(year, month)[1];
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            String yearNext = DateSet.getDateNext(year, month)[0];
            String monthNext = DateSet.getDateNext(year, month)[1];
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(UUID.fromString(managerId));
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            Map<UUID, Salary> salaryMap = new HashMap<>();
            int sumSalaryPre[] = new int[17];
            int sumSalary[] = new int[17];
            double setDouble[] = new double[16];
            double resultDouble[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
            List<Work> workList = workService.findByUserId(UUID.fromString(userId), dateFrom, dateTo);
            try {
                sumSalary = workService.calcSumSalary(UUID.fromString(userId), dateFrom, dateTo, salary.getClassSalary(), salary.getOfficeSalary(), salary.getSupportSalary());
                for (Work work : workList) {
                    setDouble = workService.calcSumSalary(work, salaryService.getByDate(UUID.fromString(userId), work.getDate()));
                    salaryMap.put(work.getId(), salaryService.getByDate(UUID.fromString(userId), work.getDate()));
                    for (int i = 0; i < 16; i++) {
                        resultDouble[i]+= setDouble[i]; 
                    }
                }
                for (int i = 0; i < 16; i++) {
                    sumSalaryPre[i] = (int)Math.ceil(resultDouble[i]);
                    if (i != 0 && i != 2 && i != 3 && i != 5 && i != 7 && i != 10 && i != 12 && i != 14) {
                        sumSalaryPre[16] += sumSalaryPre[i];
                    }
                }
                sumSalary[16] += sumSalaryPre[15] - sumSalary[15] + sumSalaryPre[13] - sumSalary[13] + sumSalaryPre[9] - sumSalary[9] + sumSalaryPre[8] - sumSalary[8] + sumSalaryPre[6] - sumSalary[6] + sumSalaryPre[4] - sumSalary[4];
                sumSalary[15] = sumSalaryPre[15];
                sumSalary[13] = sumSalaryPre[13];
                sumSalary[9] = sumSalaryPre[9];
                sumSalary[8] = sumSalaryPre[8];
                sumSalary[6] = sumSalaryPre[6];
                sumSalary[4] = sumSalaryPre[4];
                sumSalary[10] = sumSalaryPre[16] - sumSalary[16];
                sumSalary[16] += sumSalary[10];
            } catch (Exception e) {
                for (int i = 0; i < 17; i++) {
                    sumSalary[i] = 0;
                    sumSalaryPre[i]= 0; 
                }
            }
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salary", salary);
            model.addAttribute("workList", workList);
            model.addAttribute("sumSalary", sumSalary);
            model.addAttribute("salaryMap", salaryMap);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("yearBefore", yearBefore);
            model.addAttribute("monthBefore", monthBefore);
            model.addAttribute("yearNext", yearNext);
            model.addAttribute("monthNext", monthNext);
            redirectAttributes.addAttribute("manager", managerId);
            return "detailUser";
        } catch (Exception e) {
            System.out.println("Error happened in detailUser.html");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // 給与推移（講師用）
    @GetMapping("/detailSalary")
    String detailSalary(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(user.getClassAreaId());
            List<Salary> salaryList = salaryService.findByUserId(UUID.fromString(userId));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salaryList", salaryList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "detailSalary";
        } catch (Exception e) {
            System.out.println("Error happened in detailSalary.html");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // テンプレート詳細（講師用）
    @GetMapping("/detailTemplate")
    String detailTemplate(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("template") String templateId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(user.getClassAreaId());
            WorkTemplate template = workTemplateService.findTemplateById(UUID.fromString(templateId));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("template", template);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "detailTemplate";
        } catch (Exception e) {
            System.out.println("Error happened in detailTemplate.html");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // 給与推移情報（社員用）
    @GetMapping("/infoSalary")
    String infoSalary(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @Param("year") String year, @Param("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(UUID.fromString(managerId));
            List<Salary> salaryList = salaryService.findByUserId(UUID.fromString(userId));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salaryList", salaryList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "infoSalary";
        } catch (Exception e) {
            System.out.println("Error happened in infoSalary.html");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // 講師基本情報（社員用）
    @GetMapping("/infoUser")
    String infoUser(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(UUID.fromString(managerId));
            Calendar calendar = Calendar.getInstance();
            String year = String.format("%04d", calendar.get(Calendar.YEAR));
            String month = String.format("%02d", calendar.get(Calendar.MONTH)+1);
            String day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
            Salary salary = salaryService.getByDate(UUID.fromString(userId), year+"-"+month+"-"+day);
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salary", salary);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            return "infoUser";
        } catch (Exception e) {
            System.out.println("Error happened in infoUser.html");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // テンプレート一覧（講師用）
    @GetMapping("/infoTemplate")
    String infoTemplate(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(user.getClassAreaId());
            List<WorkTemplate> templateList = workTemplateService.findByUserId(UUID.fromString(userId));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("templateList", templateList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "infoTemplate";
        } catch (Exception e) {
            System.out.println("Error happened in infoTemplate.html");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // シフト登録（講師用）
    @GetMapping("/addForm")
    String addFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(user.getClassAreaId());
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            Work work = new Work();
            List<WorkTemplate> templateList = workTemplateService.findByUserId(UUID.fromString(userId));
            work.setUserId(user.getId());
            work.setCarfare(salary.getCarfare());
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("workCreateForm", work);
            model.addAttribute("templateList", templateList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "addForm";
        } catch (Exception e) {
            System.out.println("Error happened in addForm.html");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // テンプレート登録（講師用）
    @GetMapping("/templateForm")
    String templateFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(user.getClassAreaId());
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            WorkTemplate template = new WorkTemplate();
            template.setUserId(user.getId());
            template.setCarfare(salary.getCarfare());
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("templateCreateForm", template);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "templateForm";
        } catch (Exception e) {
            System.out.println("Error happened in templateForm.html");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // シフト登録（社員用）
    @GetMapping("/createForm")
    String createFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(user.getClassAreaId());
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            Work work = new Work();
            work.setUserId(user.getId());
            work.setCarfare(salary.getCarfare());
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("workCreateForm", work);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "createForm";
        } catch (Exception e) {
            System.out.println("Error happened in createForm.html");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // シフト修正（講師用）
    @GetMapping("/editForm")
    String editFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("edit") String editId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(user.getClassAreaId());
            Work work = workService.findWorkById(UUID.fromString(editId));
            List<WorkTemplate> templateList = workTemplateService.findByUserId(UUID.fromString(userId));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("workUpdateForm", work);
            model.addAttribute("templateList", templateList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "editForm";
        } catch (Exception e) {
            System.out.println("Error happened in editForm.html");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // テンプレート修正（講師用）
    @GetMapping("/editTemplateForm")
    String editTemplateFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("edit") String editId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(user.getClassAreaId());
            WorkTemplate template = workTemplateService.findTemplateById(UUID.fromString(editId));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("templateUpdateForm", template);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "editTemplateForm";
        } catch (Exception e) {
            System.out.println("Error happened in editTemplateForm.html");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // 新規給与情報登録（社員用）
    @GetMapping("/updateForm")
    String updateFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(UUID.fromString(managerId));
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salaryUpdateForm", salary);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "updateForm";
        } catch (Exception e) {
            System.out.println("Error happened in updateForm.html");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // シフト修正（社員用）
    @GetMapping("/setForm")
    String setFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("edit") String editId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(user.getClassAreaId());
            Work work = workService.findWorkById(UUID.fromString(editId));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("workUpdateForm", work);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "setForm";
        } catch (Exception e) {
            System.out.println("Error happened in setForm.html");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // 講師基本情報（講師用）
    @GetMapping("/user")
    String user(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(user.getClassAreaId());
            Calendar calendar = Calendar.getInstance();
            String yearNow = String.format("%04d", calendar.get(Calendar.YEAR));
            String monthNow = String.format("%02d", calendar.get(Calendar.MONTH)+1);
            String dayNow = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearNow+"-"+monthNow+"-"+dayNow);
            List<Salary> salaryList = salaryService.findByUserId(UUID.fromString(userId));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salary", salary);
            model.addAttribute("salaryList", salaryList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "user";
        } catch (Exception e) {
            System.out.println("Error happened in user.html");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // 講師アカウント情報修正（講師用）
    @GetMapping("/userForm")
    String userFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(user.getClassAreaId());
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salary", salary);
            model.addAttribute("userUpdateForm", user);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "userForm";
        } catch (Exception e) {
            System.out.println("Error happened in userForm.html");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // 給与情報修正（社員用）
    @GetMapping("/salaryForm")
    String salaryFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("salary") String salaryId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getById(user.getClassAreaId());
            Salary salary = salaryService.getBySalaryId(UUID.fromString(salaryId));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salaryUpdateForm", salary);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "salaryForm";
        } catch (Exception e) {
            System.out.println("Error happened in salaryForm.html");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // シフト登録（講師用）
    @PostMapping("/addForm")
    String addFormPost(@ModelAttribute("workCreateForm") Work form, RedirectAttributes redirectAttributes) {
        try {
            String year = DateSet.getYear(form.getDate());
            String month = DateSet.getMonth(form.getDate());
            String dayOfWeek = DateSet.getDayOfWeek(form.getDate());
            form.setDayOfWeek(dayOfWeek);
            form.setSupportSalary("true");
            redirectAttributes.addAttribute("user", form.getUserId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            try {
                form = workService.calcTimeAndSalary(form);
                workService.add(form);
                return "redirect:index";
            } catch (Exception e) {
                return "redirect:addForm";
            }
        } catch (Exception e) {
            System.out.println("Error happened in addForm(post)");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // テンプレート登録（講師用）
    @PostMapping("/templateForm")
    String templateFormPost(@ModelAttribute("templateCreateForm") WorkTemplate form, RedirectAttributes redirectAttributes, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            workTemplateService.add(form);
            redirectAttributes.addAttribute("user", form.getUserId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            return "redirect:infoTemplate";
        } catch (Exception e) {
            System.out.println("Error happened in templateForm(post)");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // シフト登録（社員用）
    @PostMapping("/createForm")
    String createFormPost(@ModelAttribute("workCreateForm") Work form, RedirectAttributes redirectAttributes) {
        try {
            String year = DateSet.getYear(form.getDate());
            String month = DateSet.getMonth(form.getDate());
            String dayOfWeek = DateSet.getDayOfWeek(form.getDate());
            User user = userService.getByUserId(form.getUserId());
            Manager manager = managerService.getById(user.getClassAreaId());
            form.setDayOfWeek(dayOfWeek);
            form.setSupportSalary("true");
            redirectAttributes.addAttribute("user", user.getId());
            redirectAttributes.addAttribute("manager", manager.getId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            try {
                form = workService.calcTimeAndSalary(form);
                workService.add(form);
                return "redirect:detailUser";
            } catch (Exception e) {
                return "redirect:createForm";
            }
        } catch (Exception e) {
            System.out.println("Error happened in createForm(post)");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // シフト修正（講師用）
    @PostMapping("/editForm")
    String editFormPost(@ModelAttribute("workUpdateForm") Work form, RedirectAttributes redirectAttributes) {
        try {
            String year = DateSet.getYear(form.getDate());
            String month = DateSet.getMonth(form.getDate());
            String dayOfWeek = DateSet.getDayOfWeek(form.getDate());
            User user = userService.getByUserId(form.getUserId());
            form.setDayOfWeek(dayOfWeek);
            form.setSupportSalary("true");
            redirectAttributes.addAttribute("user", user.getId());
            redirectAttributes.addAttribute("detail", form.getId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            try {
                form = workService.calcTimeAndSalary(form);
                workService.update(form);      
                return "redirect:detailWork";
            } catch (Exception e) {
                redirectAttributes.addAttribute("edit", form.getId());
                return "redirect:editForm";
            }
        } catch (Exception e) {
            System.out.println("Error happened in editForm(post)");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // シフト修正（社員用）
    @PostMapping("/setForm")
    String setFormPost(@ModelAttribute("workUpdateForm") Work form, RedirectAttributes redirectAttributes) {
        try {
            String year = DateSet.getYear(form.getDate());
            String month = DateSet.getMonth(form.getDate());
            String dayOfWeek = DateSet.getDayOfWeek(form.getDate());
            User user = userService.getByUserId(form.getUserId());
            Manager manager = managerService.getById(user.getClassAreaId());
            form.setDayOfWeek(dayOfWeek);
            form.setSupportSalary("true");
            redirectAttributes.addAttribute("user", form.getUserId());
            redirectAttributes.addAttribute("manager", manager);
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            try {
                form = workService.calcTimeAndSalary(form);
                workService.update(form);     
                return "redirect:detailUser";
            } catch (Exception e) {
                redirectAttributes.addAttribute("edit", form.getId());
                return "redirect:setForm";
            }
        } catch (Exception e) {
            System.out.println("Error happened in setForm(post)");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // シフト削除（講師用）
    @PostMapping("/deleteWork")
    String deleteWork(@Param("userId") String userId, @Param("deleteId") String deleteId, RedirectAttributes redirectAttributes) {
        try {
            Work work = workService.findWorkById(UUID.fromString(deleteId));
            String year = DateSet.getYear(work.getDate());
            String month = DateSet.getMonth(work.getDate());
            workService.deleteById(UUID.fromString(deleteId));
            redirectAttributes.addAttribute("user", userId);
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            return "redirect:index";
        } catch (Exception e) {
            System.out.println("Error happened in deleteWork(post)");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // シフト削除（社員用）
    @PostMapping("/clearWork")
    String clearWork(@Param("managerId") String managerId, @Param("userId") String userId, @Param("deleteId") String deleteId, RedirectAttributes redirectAttributes) {
        try {
            Work work = workService.findWorkById(UUID.fromString(deleteId));
            String year = DateSet.getYear(work.getDate());
            String month = DateSet.getMonth(work.getDate());
            workService.deleteById(UUID.fromString(deleteId));
            redirectAttributes.addAttribute("manager", managerId);
            redirectAttributes.addAttribute("user", userId);
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            return "redirect:detailUser";
        } catch (Exception e) {
            System.out.println("Error happened in clearWork(post)");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // 講師アカウント情報修正（講師用）
    @PostMapping("/userForm")
    String userFormPost(@ModelAttribute("userUpdateForm") User form, RedirectAttributes redirectAttributes) {
        try {
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            userService.update(form);
            redirectAttributes.addAttribute("user", form.getId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            return "redirect:user";
        } catch (Exception e) {
            System.out.println("Error happened in userForm(post)");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // 新規給与情報登録（社員用）
    @PostMapping("/updateForm")
    String updateFormPost(@ModelAttribute("salaryUpdateForm") Salary form, RedirectAttributes redirectAttributes) {
        try {
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            User user = userService.getByUserId(form.getUserId());
            Manager manager = managerService.getById(user.getClassAreaId());
            salaryService.add(form);
            redirectAttributes.addAttribute("user", user.getId());
            redirectAttributes.addAttribute("manager", manager.getId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            return "redirect:infoUser";
        } catch (Exception e) {
            System.out.println("Error happened in updateForm(post)");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // 給与情報修正（社員用）
    @PostMapping("/salaryForm")
    String salaryFormPost(@ModelAttribute("salaryUpdateForm") Salary form, RedirectAttributes redirectAttributes) {
        try {
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            User user = userService.getByUserId(form.getUserId());
            Manager manager = managerService.getById(user.getClassAreaId());
            salaryService.update(form);
            redirectAttributes.addAttribute("user", user.getId());
            redirectAttributes.addAttribute("manager", manager.getId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            return "redirect:infoSalary";
        } catch (Exception e) {
            System.out.println("Error happened in salaryForm(post)");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // テンプレート修正（講師用）
    @PostMapping("/editTemplateForm")
    String editTemplateFormPost(@ModelAttribute("templateUpdateForm") WorkTemplate form, RedirectAttributes redirectAttributes, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            workTemplateService.update(form);
            redirectAttributes.addAttribute("user", form.getUserId());
            redirectAttributes.addAttribute("template", form.getId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            return "redirect:detailTemplate";
        } catch (Exception e) {
            System.out.println("Error happened in editTemplateForm(post)");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // テンプレート削除（講師用）
    @PostMapping("/deleteTemplate")
    String deleteTemplate(RedirectAttributes redirectAttributes, @RequestParam("deleteId") String deleteId, @RequestParam("userId") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            workTemplateService.deleteById(UUID.fromString(deleteId));
            redirectAttributes.addAttribute("user", userId);
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            return "redirect:infoTemplate";
        } catch (Exception e) {
            System.out.println("Error happened in deleteTemplate(post)");
            e.printStackTrace();
            return "redirect:login";
        }
    }

    // 給与情報削除（社員用）
    @PostMapping("/deleteSalary")
    public String deleteSalary(RedirectAttributes redirectAttributes, @RequestParam("deleteId") String deleteId, @RequestParam("userId") String userId, @RequestParam("managerId") String managerId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            salaryService.delete(salaryService.getBySalaryId(UUID.fromString(deleteId)));
            redirectAttributes.addAttribute("user", userId);
            redirectAttributes.addAttribute("manager", managerId);
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            return "redirect:infoSalary";
        } catch (Exception e) {
            System.out.println("Error happened in deleteSalary(post)");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    
    // 講師ログイン（講師用）
    @GetMapping("/login")
    String loginGet(Model model) {
        try {
            model.addAttribute("userLoginForm", new User());
            return "login";
        } catch (Exception e) {
            System.out.println("Error happened in login.html");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // 社員ログイン（社員用）
    @GetMapping("/loginManager")
    String loginManagerGet(Model model) {
        try {
            model.addAttribute("managerLoginForm", new Manager());
            return "loginManager";
        } catch (Exception e) {
            System.out.println("Error happened in loginManager.html");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // 講師ログイン（講師用）
    @PostMapping("/login")
    String loginPost(@ModelAttribute("userLoginForm") User loginUser, RedirectAttributes redirectAttributes) {
        try {
            User trueUser = userService.getByLoginId(loginUser.getLoginId());
            if (trueUser == null || !trueUser.getPassword().equals(loginUser.getPassword())) {
                return "redirect:login";
            } else {
                UUID userId = trueUser.getId();                        
                Calendar calendar = Calendar.getInstance();
                String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
                String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
                redirectAttributes.addAttribute("user", userId);
                redirectAttributes.addAttribute("year", year);
                redirectAttributes.addAttribute("month", month);
                return "redirect:index";
            }
        } catch (Exception e) {
            System.out.println("Error happened in login(post)");
            e.printStackTrace();
            return "redirect:login";
        }
    }
    
    // 社員ログイン（社員用）
    @PostMapping("/loginManager")
    String loginManagerPost(@ModelAttribute("managerLoginForm") Manager loginManager, RedirectAttributes redirectAttributes) {
        try {
            Manager trueManager = managerService.getByLoginId(loginManager.getLoginId());
            if (trueManager == null || !trueManager.getPassword().equals(loginManager.getPassword())) {
                return "redirect:loginManager";
            } else {
                UUID managerId = trueManager.getId();
                redirectAttributes.addAttribute("manager", managerId);
                return "redirect:indexManager";
            }
        } catch (Exception e) {
            System.out.println("Error happened in loginManager(post)");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // 講師アカウント作成（社員用）
    @GetMapping("/signUp")
    String signUpGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId) {
        try {
            Manager manager = managerService.getById(UUID.fromString(managerId));
            User user = new User();
            Salary salary = new Salary();
            user.setClassAreaId(manager.getId());
            model.addAttribute("manager", manager);
            model.addAttribute("userCreateForm", user);
            model.addAttribute("salaryCreateForm", salary);
            return "signUp";
        } catch (Exception e) {
            System.out.println("Error happened in signUp.html");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // 社員アカウント作成（社員用）
    @GetMapping("/signUpManager")
    String signUpManagerGet(Model model) {
        try {
            model.addAttribute("managerCreateForm", new Manager());
            return "signUpManager";
        } catch (Exception e) {
            System.out.println("Error happened in signUpManager.html");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // 講師アカウント作成（社員用）
    @PostMapping("/signUp")
    String signUpPost(@ModelAttribute("userCreateForm") User user, @ModelAttribute("salaryCreateForm") Salary salary, RedirectAttributes redirectAttributes) {
        try {
            userService.add(user);
            User setUser = userService.getByLoginId(user.getLoginId());
            salary.setUserId(setUser.getId());
            salaryService.add(salary);
            redirectAttributes.addAttribute("manager", user.getClassAreaId());
            return "redirect:indexManager";
        } catch (Exception e) {
            System.out.println("Error happened in signUp(post)");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
    // 社員アカウント作成（社員用）
    @PostMapping("/signUpManager")
    String signUpManagerPost(@ModelAttribute("managerCreateForm") Manager manager) {
        try {
            managerService.add(manager.getLoginId(), manager.getPassword(), manager.getClassArea());
            return "redirect:loginManager";
        } catch (Exception e) {
            System.out.println("Error happened in signUpManager(post)");
            e.printStackTrace();
            return "redirect:loginManager";
        }
    }
    
}
