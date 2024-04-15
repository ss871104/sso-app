import './LoginButton.css';

interface ButtonProps {
  onClick: () => void;
  children: React.ReactNode;
}

const FacebookButton: React.FC<ButtonProps> = ({ onClick, children }) => {
  return (
    <button onClick={onClick} className="login-btn login-with-facebook-btn" >
      {children}
    </button>
  );
};

export default FacebookButton;
