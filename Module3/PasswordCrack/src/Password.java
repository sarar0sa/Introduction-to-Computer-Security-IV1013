public class Password {
    private String password, salt, user;

    Password(String password, String user){
        this.password = password;
        this.user = user;
        salt = password.substring(0,2);

    }

    public String getPassword(){
        return password;
    }

    public String getUser(){
        return user;
    }

    public String getSalt(){
        return salt;
    }

}
