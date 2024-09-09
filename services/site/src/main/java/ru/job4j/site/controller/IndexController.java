package ru.job4j.site.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.job4j.site.dto.InterviewDTO;
import ru.job4j.site.dto.TopicDTO;
import ru.job4j.site.service.*;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.job4j.site.controller.RequestResponseTools.getToken;

@Controller
@AllArgsConstructor
@Slf4j
public class IndexController {
    private final CategoriesService categoriesService;
    private final InterviewsService interviewsService;
    private final AuthService authService;
    private final NotificationService notifications;
    private final ProfilesService profilesService;
    private final TopicsService topicsService;

    @GetMapping({"/", "index"})
    public String getIndexPage(Model model, HttpServletRequest req) throws JsonProcessingException {
        RequestResponseTools.addAttrBreadcrumbs(model,
                "Главная", "/"
        );
        try {
            model.addAttribute("categories", categoriesService.getMostPopular());
            var token = getToken(req);
            if (token != null) {
                var userInfo = authService.userInfo(token);
                model.addAttribute("userInfo", userInfo);
                model.addAttribute("userDTO", notifications.findCategoriesByUserId(userInfo.getId()));
                RequestResponseTools.addAttrCanManage(model, userInfo);
            }
        } catch (Exception e) {
            log.error("Remote application not responding. Error: {}. {}, ", e.getCause(), e.getMessage());
        }
        List<InterviewDTO> newInterviews = interviewsService.getByType(1);
        model.addAttribute("new_interviews", newInterviews);
        model.addAttribute("usernames", getUsernamesFromInterviews(newInterviews));
        model.addAttribute("countInterviewsByCategories", getCountOfInterviewsByCategory(newInterviews));
        return "index";
    }

    protected Map<Integer, String> getUsernamesFromInterviews(List<InterviewDTO> interviews) {
        Map<Integer, String> usernames = new HashMap<>();
        interviews.forEach(i -> usernames.put(
                i.getSubmitterId(),
                profilesService.getProfileById(i.getSubmitterId()).get().getUsername())
        );
        return usernames;
    }

    protected Map<Integer, Integer> getCountOfInterviewsByCategory(List<InterviewDTO> interviews)
            throws JsonProcessingException {
        Map<Integer, Integer> countInterviewsByCategories = new HashMap<>();
        for (InterviewDTO interview : interviews) {
            TopicDTO topicDTO = topicsService.getById(interview.getTopicId());
            countInterviewsByCategories.merge(topicDTO.getCategory().getId(), 1, Integer::sum);
        }
        return countInterviewsByCategories;
    }
}