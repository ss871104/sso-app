import './LoginButton.css';

interface ButtonProps {
  onClick: () => void;
  children: React.ReactNode;
}

const GoogleButton: React.FC<ButtonProps> = ({ onClick, children }) => {
  return (
    <button onClick={onClick} className="login-btn login-with-google-btn" >
      {children}
    </button>
  );
};

export default GoogleButton;
