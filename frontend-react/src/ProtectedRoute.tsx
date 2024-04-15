import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

interface ProtectedRouteProps {
    children: React.ReactNode;
}
  
const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
    const navigate = useNavigate();

    useEffect(() => {
        fetch('http://localhost/auth-service/api/auth/authenticate', {
          credentials: 'include'
        })
        .then(response => response.json())
        .then(data => {
          if (data.error) {
            alert(data.message);
            navigate('/login');
          }
        })
        .catch(error => {
          console.error('Error:', error)
        });
      }, [navigate]);
  
    return <>{children}</>;
};
  
export default ProtectedRoute;