package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.EnviarChatDto;
import ar.edu.utn.frc.tup.piii.Services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/enviar")
    public ResponseEntity<Void> enviar(@RequestBody EnviarChatDto dto) {
        chatService.enviar(dto);
        return ResponseEntity.ok().build();
    }
}
