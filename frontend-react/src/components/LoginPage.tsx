import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import GoogleButton from "./GoogleButton";
import "./LoginPage.css"
import FacebookButton from "./FacebookButton";

const LoginPage: React.FC = () => {
    const navigate = useNavigate();

    useEffect(() => {
        fetch('http://localhost/auth-service/api/auth/authenticate', {
          credentials: 'include'
        })
        .then(response => response.json())
        .then(data => {
          if (data.isAuthenticated) {
            alert("Already login, navigating to home page!")
            navigate('/');
          }
        })
        .catch(error => {
          console.error('Error:', error)
        });
      }, [navigate]);

    const handleGoogleLogin = () => {
        window.location.href = 'http://localhost:8000/oauth2/authorization/google';
    };
    const handleFacebookLogin = () => {
      window.location.href = 'http://localhost:8000/oauth2/authorization/facebook';
  };

    return (
      <div className="login-container">
        <div className="login-box">
          <div className="login-title">Welcome!<br /> You can login via...</div>
          <GoogleButton onClick={handleGoogleLogin}>Sign in with Google</GoogleButton>
          <FacebookButton onClick={ handleFacebookLogin }>Sign in with Facebook</FacebookButton>
        </div>
      </div>
    );
};
export default LoginPage;
