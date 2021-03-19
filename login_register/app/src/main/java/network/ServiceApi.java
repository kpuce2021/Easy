package network;

import data.JoinData;
import data.JoinResponse;
import data.LoginData;
import data.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/*API 인터페이스 정의
* 필요한 통신이 새로운 사용자 정보를 삽입할 수 있는 join과 사용자의 이메일 + 비밀번호를 대조해 볼 수 있는 login임.
* 인터페이스를 하나 만들어 서버에 어떤식으로 요청을 보내고 응답을 받을 것이지 미리 정의해야함.
* 내 node.js 에서 작성한 회원가입,로그인 API에 맞춰 작성.
* */

public interface ServiceApi {
    /*
    * 두가지 모두 POST 방식이 필요하므로 @POST 어노테이션을 사용
    * userLogin 함수는 request 통신 시 email과 password를 필드로 가짐 / reponse 타입은 Call<UserData>
    * userJoin 함수는 request 통신시 UserData 타입을 body로 가짐 / reponse 타입은 Call<ResponseBody>
    * */
    @POST("/user/login")
    Call<LoginResponse> userLogin(@Body LoginData data);

    @POST("/user/join")
    Call<JoinResponse> userJoin(@Body JoinData data);
}
