import { useEffect } from "react";
import { useLocation } from "react-router-dom";

const ErrorPage: React.FC = () => {
    const location = useLocation();

    useEffect(() => {
        const queryParams = new URLSearchParams(location.search);
        const errorMessage = queryParams.get('message');

        if (errorMessage) {
            alert(`${errorMessage}`);
            window.location.href = "/login";
        }
    }, [location]);

    return (
        <div></div>
    );
};
export default ErrorPage;
