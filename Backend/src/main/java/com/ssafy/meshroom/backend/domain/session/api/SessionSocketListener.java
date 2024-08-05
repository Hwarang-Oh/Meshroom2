package com.ssafy.meshroom.backend.domain.session.api;

import com.ssafy.meshroom.backend.domain.OVToken.application.OVTokenService;
import com.ssafy.meshroom.backend.domain.session.application.SessionService;
import com.ssafy.meshroom.backend.domain.session.domain.Session;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionSocketListener {

    private final SessionService sessionService;
    private final OVTokenService ovTokenService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @EventListener
    public void sessionConnectedEventHandler(SessionConnectedEvent event) throws OpenViduJavaClientException, OpenViduHttpException {
        // session service의 session info 보내기
        Principal p = event.getUser();
        Session session = ovTokenService.getMainSessionFromUserId(p.getName());
        // session service의 session info 보내기
        sendMessage(session);

        System.out.println("SessionConnectedEvent = " + event);
    }

    @EventListener
    public void sessionDisconnectEventHandler(SessionDisconnectEvent event) throws OpenViduJavaClientException, OpenViduHttpException {
        Principal p = event.getUser();
        Session session = ovTokenService.getMainSessionFromUserId(p.getName());

        // sessionId 에 해당하는 토큰 삭제 ovTokenService 에서 삭제
        ovTokenService.removeSession(session.get_id());

        // session service의 session info 보내기
        sendMessage(session);

        System.out.println("SessionDisconnectEvent = " + event);
    }

    private void sendMessage(Session session) throws OpenViduJavaClientException, OpenViduHttpException {
        boolean isMain = session.getIsMain();
        if(isMain){
            String sessionId = session.getSessionId();
            simpMessagingTemplate.convertAndSend("/subscribe/sessions/" + sessionId, sessionService.getSessionInfo(sessionId).getResult());
        }else{
            String mainSessionId = session.getMainSession();
            simpMessagingTemplate.convertAndSend("/subscribe/sessions/" + mainSessionId, sessionService.getSessionInfo(mainSessionId).getResult());
        }
    }

}
