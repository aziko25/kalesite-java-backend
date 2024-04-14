package kalesite.kalesite.Services;

import kalesite.kalesite.Models.Orders.Order_Projects;
import kalesite.kalesite.Repositories.Orders.Order_ProjectsRepository;
import kalesite.kalesite.Telegram.MainTelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@RequiredArgsConstructor
public class ProjectsOrderService {

    @Value("${orders_chat_id}")
    private String chatId;

    private final MainTelegramBot mainTelegramBot;

    private final Order_ProjectsRepository order_projectsRepository;

    public String saveProject(String fullName, String phone, String description) {

        Order_Projects project = new Order_Projects();

        project.setFullName(fullName);
        project.setPhone(phone);
        project.setDescription(description);

        order_projectsRepository.save(project);

        SendMessage message = new SendMessage();

        message.setChatId(chatId);
        message.setText("Тип: Заказать Проект\n-----------------\nПолное Имя: " + fullName + "\nТел: " + phone + "\nОписание: " + description);

        mainTelegramBot.sendMessage(message);

        return "Success!";
    }
}