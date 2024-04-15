import './App.css';
import { Route, Routes } from 'react-router-dom';
import HomePage from './components/HomePage';
import LoginPage from './components/LoginPage';
import ProtectedRoute from './ProtectedRoute';
import ErrorPage from './components/ErrorPage';

const App: React.FC = () => {
  return (
    <Routes>
      <Route path="/" element={
        <ProtectedRoute>
          <HomePage />
        </ProtectedRoute>
      } />
      <Route path='/login' element={<LoginPage />}/>
      <Route path='/error' element={<ErrorPage />}/>
    </Routes>
  );
};

export default App;
