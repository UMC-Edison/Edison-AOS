import { BrowserRouter, Routes, Route } from 'react-router-dom';
import BubbleInputPage from './screens/BubbleInputPage/BubbleInputPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<BubbleInputPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;