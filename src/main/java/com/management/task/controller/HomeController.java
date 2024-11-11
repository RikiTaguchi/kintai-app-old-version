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
    }
    
    // 社員ホーム画面
    @GetMapping("/indexManager")
    String indexManager(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId) {
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
    }
    
    // 給与詳細（講師用）
    @GetMapping("/detail")
    String detail(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
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
    }
    
    // 勤務詳細（講師用）
    @GetMapping("/detailWork")
    String detailWork(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("detail") String detailId, @RequestParam("year") String year, @RequestParam("month") String month) {
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
    }
    
    // 給与詳細（社員用）
    @GetMapping("/detailUser")
    String detailUser(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @Param("year") String year, @Param("month") String month) {
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
    }
    
    // 給与推移（講師用）
    @GetMapping("/detailSalary")
    String detailSalary(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
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
    }
    
    // テンプレート詳細（講師用）
    @GetMapping("/detailTemplate")
    String detailTemplate(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("template") String templateId, @RequestParam("year") String year, @RequestParam("month") String month) {
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
    }
    
    // 給与推移情報（社員用）
    @GetMapping("/infoSalary")
    String infoSalary(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @Param("year") String year, @Param("month") String month) {
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
    }
    
    // 講師基本情報（社員用）
    @GetMapping("/infoUser")
    String infoUser(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId) {
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
    }
    
    // テンプレート一覧（講師用）
    @GetMapping("/infoTemplate")
    String infoTemplate(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
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
    }
    
    // シフト登録（講師用）
    @GetMapping("/addForm")
    String addFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
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
    }
    
    // テンプレート登録（講師用）
    @GetMapping("/templateForm")
    String templateFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
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
    }
    
    // シフト登録（社員用）
    @GetMapping("/createForm")
    String createFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
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
    }
    
    // シフト修正（講師用）
    @GetMapping("/editForm")
    String editFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("edit") String editId, @RequestParam("year") String year, @RequestParam("month") String month) {
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
    }
    
    // テンプレート修正（講師用）
    @GetMapping("/editTemplateForm")
    String editTemplateFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("edit") String editId, @RequestParam("year") String year, @RequestParam("month") String month) {
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
    }
    
    // 新規給与情報登録（社員用）
    @GetMapping("/updateForm")
    String updateFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
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
    }
    
    // シフト修正（社員用）
    @GetMapping("/setForm")
    String setFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("edit") String editId, @RequestParam("year") String year, @RequestParam("month") String month) {
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
    }
    
    // 講師基本情報（講師用）
    @GetMapping("/user")
    String user(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
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
    }
    
    // 講師アカウント情報修正（講師用）
    @GetMapping("/userForm")
    String userFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
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
    }
    
    // 給与情報修正（社員用）
    @GetMapping("/salaryForm")
    String salaryFormGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("salary") String salaryId, @RequestParam("year") String year, @RequestParam("month") String month) {
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
    }
    
    // シフト登録（講師用）
    @PostMapping("/addForm")
    String addFormPost(@ModelAttribute("workCreateForm") Work form, RedirectAttributes redirectAttributes) {
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
    }
    
    // テンプレート登録（講師用）
    @PostMapping("/templateForm")
    String templateFormPost(@ModelAttribute("templateCreateForm") WorkTemplate form, RedirectAttributes redirectAttributes, @RequestParam("year") String year, @RequestParam("month") String month) {
        workTemplateService.add(form);
        redirectAttributes.addAttribute("user", form.getUserId());
        redirectAttributes.addAttribute("year", year);
        redirectAttributes.addAttribute("month", month);
        return "redirect:infoTemplate";
    }
    
    // シフト登録（社員用）
    @PostMapping("/createForm")
    String createFormPost(@ModelAttribute("workCreateForm") Work form, RedirectAttributes redirectAttributes) {
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
    }
    
    // シフト修正（講師用）
    @PostMapping("/editForm")
    String editFormPost(@ModelAttribute("workUpdateForm") Work form, RedirectAttributes redirectAttributes) {
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
    }
    
    // シフト修正（社員用）
    @PostMapping("/setForm")
    String setFormPost(@ModelAttribute("workUpdateForm") Work form, RedirectAttributes redirectAttributes) {
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
    }
    
    // シフト削除（講師用）
    @PostMapping("/deleteWork")
    String deleteWork(@Param("userId") String userId, @Param("deleteId") String deleteId, RedirectAttributes redirectAttributes) {
        Work work = workService.findWorkById(UUID.fromString(deleteId));
        String year = DateSet.getYear(work.getDate());
        String month = DateSet.getMonth(work.getDate());
        workService.deleteById(UUID.fromString(deleteId));
        redirectAttributes.addAttribute("user", userId);
        redirectAttributes.addAttribute("year", year);
        redirectAttributes.addAttribute("month", month);
        return "redirect:index";
    }
    
    // シフト削除（社員用）
    @PostMapping("/clearWork")
    String clearWork(@Param("managerId") String managerId, @Param("userId") String userId, @Param("deleteId") String deleteId, RedirectAttributes redirectAttributes) {
        Work work = workService.findWorkById(UUID.fromString(deleteId));
        String year = DateSet.getYear(work.getDate());
        String month = DateSet.getMonth(work.getDate());
        workService.deleteById(UUID.fromString(deleteId));
        redirectAttributes.addAttribute("manager", managerId);
        redirectAttributes.addAttribute("user", userId);
        redirectAttributes.addAttribute("year", year);
        redirectAttributes.addAttribute("month", month);
        return "redirect:detailUser";
    }
    
    // 講師アカウント情報修正（講師用）
    @PostMapping("/userForm")
    String userFormPost(@ModelAttribute("userUpdateForm") User form, RedirectAttributes redirectAttributes) {
        Calendar calendar = Calendar.getInstance();
        String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
        String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
        userService.update(form);
        redirectAttributes.addAttribute("user", form.getId());
        redirectAttributes.addAttribute("year", year);
        redirectAttributes.addAttribute("month", month);
        return "redirect:user";
    }
    
    // 新規給与情報登録（社員用）
    @PostMapping("/updateForm")
    String updateFormPost(@ModelAttribute("salaryUpdateForm") Salary form, RedirectAttributes redirectAttributes) {
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
    }
    
    // 給与情報修正（社員用）
    @PostMapping("/salaryForm")
    String salaryFormPost(@ModelAttribute("salaryUpdateForm") Salary form, RedirectAttributes redirectAttributes) {
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
    }
    
    // テンプレート修正（講師用）
    @PostMapping("/editTemplateForm")
    String editTemplateFormPost(@ModelAttribute("templateUpdateForm") WorkTemplate form, RedirectAttributes redirectAttributes, @RequestParam("year") String year, @RequestParam("month") String month) {
        workTemplateService.update(form);
        redirectAttributes.addAttribute("user", form.getUserId());
        redirectAttributes.addAttribute("template", form.getId());
        redirectAttributes.addAttribute("year", year);
        redirectAttributes.addAttribute("month", month);
        return "redirect:detailTemplate";
    }
    
    // テンプレート削除（講師用）
    @PostMapping("/deleteTemplate")
    String deleteTemplate(RedirectAttributes redirectAttributes, @RequestParam("deleteId") String deleteId, @RequestParam("userId") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        workTemplateService.deleteById(UUID.fromString(deleteId));
        redirectAttributes.addAttribute("user", userId);
        redirectAttributes.addAttribute("year", year);
        redirectAttributes.addAttribute("month", month);
        return "redirect:infoTemplate";
    }
    
    // 講師ログイン
    @GetMapping("/login")
    String loginGet(Model model) {
        model.addAttribute("userLoginForm", new User());
        return "login";
    }
    
    // 社員ログイン
    @GetMapping("/loginManager")
    String loginManagerGet(Model model) {
        model.addAttribute("managerLoginForm", new Manager());
        return "loginManager";
    }
    
    // 講師ログイン
    @PostMapping("/login")
    String loginPost(@ModelAttribute("userLoginForm") User loginUser, RedirectAttributes redirectAttributes) {
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
    }
    
    // 社員ログイン
    @PostMapping("/loginManager")
    String loginManagerPost(@ModelAttribute("managerLoginForm") Manager loginManager, RedirectAttributes redirectAttributes) {
        Manager trueManager = managerService.getByLoginId(loginManager.getLoginId());
        if (trueManager == null || !trueManager.getPassword().equals(loginManager.getPassword())) {
            return "redirect:loginManager";
        } else {
            UUID managerId = trueManager.getId();
            redirectAttributes.addAttribute("manager", managerId);
            return "redirect:indexManager";
        }
    }
    
    // 講師アカウント作成
    @GetMapping("/signUp")
    String signUpGet(Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId) {
        Manager manager = managerService.getById(UUID.fromString(managerId));
        User user = new User();
        Salary salary = new Salary();
        user.setClassAreaId(manager.getId());
        model.addAttribute("manager", manager);
        model.addAttribute("userCreateForm", user);
        model.addAttribute("salaryCreateForm", salary);
        return "signUp";
    }
    
    // 社員アカウント作成
    @GetMapping("/signUpManager")
    String signUpManagerGet(Model model) {
        model.addAttribute("managerCreateForm", new Manager());
        return "signUpManager";
    }
    
    // 講師アカウント作成
    @PostMapping("/signUp")
    String signUpPost(@ModelAttribute("userCreateForm") User user, @ModelAttribute("salaryCreateForm") Salary salary, RedirectAttributes redirectAttributes) {
        userService.add(user);
        User setUser = userService.getByLoginId(user.getLoginId());
        salary.setUserId(setUser.getId());
        salaryService.add(salary);
        redirectAttributes.addAttribute("manager", user.getClassAreaId());
        return "redirect:indexManager";
    }
    
    // 社員アカウント作成
    @PostMapping("/signUpManager")
    String signUpManagerPost(@ModelAttribute("managerCreateForm") Manager manager) {
        managerService.add(manager.getLoginId(), manager.getPassword(), manager.getClassArea());
        return "redirect:loginManager";
    }
    
}
