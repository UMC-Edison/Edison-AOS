import styled from 'styled-components';
import { useNavigate } from 'react-router-dom';
import { useEffect, useState, useRef } from 'react';
import BubbleDoor from '../../components/BubbleInputPage/BubbleInput';
import Menu from '../../components/Menu';

import { v4 as uuidv4 } from 'uuid';


const gradientColors = ['red'];


const BubbleInputPage = () => {
  const navigate = useNavigate();
  const blockRefs = useRef({});
  const [isMoreMenu, setIsMoreMenu] = useState(false);
  const [title, setTitle] = useState('');
  const [keyboardHeight, setKeyboardHeight] = useState(0);
  const [contentBlocks, setContentBlocks] = useState([
    { id: uuidv4(), type: 'text', content: '' },
  ]);
  const initialHeight = useRef(window.innerHeight);
  const [isTextStyleOpen, setIsTextStyleOpen] = useState(false);
  const [isBold, setIsBold] = useState(false);
  const [isItalic, setIsItalic] = useState(false);
  const [isUnderline, setIsUnderline] = useState(false);
  const [isHighlight, setIsHighlight] = useState(false);

  const [isListStyleOpen, setIsListStyleOpen] = useState(false);
  const [isList, setIsList] = useState(false);
  const [isNumber, setIsNumber] = useState(false);

  const [isCameraMenuOpen, setIsCameraMenuOpen] = useState(false);

  const [focusedBlockId, setFocusedBlockId] = useState(null);


  const handleTextChange = (id, newText) => {
    setContentBlocks(blocks =>
      blocks.map(block =>
        block.id === id ? { ...block, content: newText } : block
      )
    );
  };

  const handleImageInsert = (index, imageUrl) => {
    const newBlock = { id: uuidv4(), type: 'image', content: imageUrl };
    setContentBlocks(prev => {
      const newBlocks = [...prev];
      newBlocks.splice(index + 1, 0, newBlock);
      return newBlocks;
    });
  };

  const applyTextStyle = (command) => {
  if (document.queryCommandSupported(command)) {
    document.execCommand(command, false, null);
  }
};

const applyHighlight = () => {
  const selection = window.getSelection();
  if (!selection || selection.rangeCount === 0) return;

  const activeElement = document.activeElement;
  if (activeElement && activeElement.contentEditable === 'true') {
    activeElement.focus();

    document.execCommand("styleWithCSS", false, "true"); // 이 줄 추가!
    document.execCommand('backColor', false, isHighlight ? 'transparent' : '#FFFF00');
    setIsHighlight(!isHighlight);
  }
};

const applyUnorderedList = () => {
  const selection = window.getSelection();
  const activeElement = document.activeElement;

  if (!selection || selection.rangeCount === 0) {
    // 현재 활성 블록 focus (없을 경우)
    const currentBlock = Object.values(blockRefs.current).find(el => el?.contains(document.activeElement));
    currentBlock?.focus();
  }

  setTimeout(() => {
    document.execCommand('insertUnorderedList');
    setIsList(prev => !prev);
    setIsNumber(false); // 숫자 리스트 꺼짐
  }, 0);
};

const applyOrderedList = () => {
  const selection = window.getSelection();
  const activeElement = document.activeElement;

  if (!selection || selection.rangeCount === 0) {
    const currentBlock = Object.values(blockRefs.current).find(el => el?.contains(document.activeElement));
    currentBlock?.focus();
  }

  setTimeout(() => {
    document.execCommand('insertOrderedList');
    setIsNumber(prev => !prev); // 숫자 리스트 토글
    setIsList(false); // 일반 리스트 해제
  }, 0);
};


const handleContentEdit = (id, html) => {
  setContentBlocks(prev =>
    prev.map(block =>
      block.id === id ? { ...block, content: html } : block
    )
  );
};

const handleImageSelect = (capture = false) => {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'image/*';
  if (capture) input.capture = 'environment'; // 후면 카메라 촬영 (모바일만 지원)

  input.onchange = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      const imageUrl = URL.createObjectURL(file);
      insertImageAtFocusedBlock(imageUrl);
    }
  };

  input.click();
};

const insertImageAtFocusedBlock = (imageUrl) => {
  const index = contentBlocks.findIndex(block => block.id === focusedBlockId);
  if (index !== -1) {
    handleImageInsert(index, imageUrl);
  } else {
    // 포커스된 블록이 없으면 맨 마지막에 추가
    handleImageInsert(contentBlocks.length - 1, imageUrl);
  }
};


  useEffect(() => {
  window.addEventListener('resize', handleResize);
  return () => window.removeEventListener('resize', handleResize);
}, []);

useEffect(() => {
  const handleSelectionChange = () => {
    setIsBold(document.queryCommandState('bold'));
    setIsItalic(document.queryCommandState('italic'));
    setIsUnderline(document.queryCommandState('underline'));
    setIsList(document.queryCommandState('insertUnorderedList'));
    setIsNumber(document.queryCommandState('insertOrderedList'));
  };

  document.addEventListener('selectionchange', handleSelectionChange);
  return () => document.removeEventListener('selectionchange', handleSelectionChange);
}, []);


const handleResize = () => {
    const newHeight = window.innerHeight;
    const heightDiff = initialHeight - newHeight;
    setKeyboardHeight(heightDiff > 150 ? heightDiff : 0);
  };

  const menuItems = [
  { label: '공유', textColor: '#3A3D40', onClick: () => {} },
  { label: '삭제', textColor: '#FF0000', onClick: () => {} },
];

const cameraItems = [
  {
    label: '사진 촬영',
    textColor: '#3A3D40',
    onClick: () => {
      setIsCameraMenuOpen(false);
      handleImageSelect(true);
    },
  },
  {
    label: '갤러리',
    textColor: '#3A3D40',
    onClick: () => {
      setIsCameraMenuOpen(false);
      handleImageSelect(false);
    },
  },
];


  return (
    <Page>
      <Header>
        <Left onClick={() => navigate(-1)}>
          <img src="/assets/images/back.svg" style={{ width: 24, height: 24 }} />
        </Left>
        <Right>
          <MenuButton>
            <img
              src="/assets/images/more.svg"
              style={{ width: 32, height: 32 }}
              onClick={() => setIsMoreMenu(!isMoreMenu)}
            />
          </MenuButton>
          <img
            src="/assets/images/save.svg"
            style={{ width: 32, height: 34 }}
            onClick={() => console.log('save data')}
          />
        </Right>
      </Header>

      <BubbleDoor outerColors={gradientColors}>
        <TitleInput
          value={title}
          onChange={e => setTitle(e.target.value)}
          placeholder="제목 입력"
        />
        {contentBlocks.map((block, index) => (
          <div key={block.id}>
            {block.type === 'text' ? (
              <TextInput
  contentEditable
  placeholder="내용 입력"
  ref={el => (blockRefs.current[block.id] = el)}
  tabIndex={0}
  onFocus={() => setFocusedBlockId(block.id)}
  onInput={() =>
    handleContentEdit(
      block.id,
      blockRefs.current[block.id]?.innerHTML || ''
    )
  }
/>

            ) : (
              <ImagePreview src={block.content} alt="업로드된 이미지" />
            )}
            
          </div>
        ))}
      </BubbleDoor>
      {/* {keyboardHeight > 0 && !isTextStyleOpen && (
  <KeyboardToolbar style={{ bottom: `${keyboardHeight}px` }}>
    <img src='/assets/images/text_tool_off.svg' />
    <img src='/assets/images/list_tool_off.svg' />
    <img src='/assets/images/camera_tool_off.svg' />
    <img src='/assets/images/link_tool_off.svg' />
    <img src='/assets/images/label_tool_off.svg' />
  </KeyboardToolbar>
)} */}
<KeyboardToolbar style={{ bottom: `${keyboardHeight}px` }}>
    <img src='/assets/images/text_tool_off.svg' onClick={() => {setIsTextStyleOpen(!isTextStyleOpen)}}/>
    <img src='/assets/images/list_tool_off.svg' onClick={() => {setIsListStyleOpen(!isListStyleOpen)}}/>
    <div style={{ position: 'relative', display: 'inline-block' }}>
  <img
    src={isCameraMenuOpen ? '/assets/images/camera_tool_on.svg' : '/assets/images/camera_tool_off.svg'}
    onClick={() => { setIsCameraMenuOpen(!isCameraMenuOpen); }}
  />
  {isCameraMenuOpen && <Menu items={cameraItems} />}
</div>

    <img src='/assets/images/link_tool_off.svg' />
    <img src='/assets/images/label_tool_off.svg' />
  </KeyboardToolbar>
  {isTextStyleOpen && <KeyboardToolbar style={{ bottom: `${keyboardHeight}px` }}>
    <img
  src={isBold ? '/assets/images/bold.svg' : '/assets/images/bold_unclicked.svg'}
  onClick={() => {
    applyTextStyle('bold');
    setIsBold(!isBold);
  }}
/>
    <img src={isItalic ? '/assets/images/italic.svg' : '/assets/images/italic_unclicked.svg'} onClick={() => {
    applyTextStyle('italic');
    setIsItalic(!isItalic);
  }} />
    <img src={isUnderline ? '/assets/images/underline.svg' : '/assets/images/underline_unclicked.svg'} onClick={() => {applyTextStyle('underline'); setIsUnderline(!isUnderline)}} />
    <img src={isHighlight ? '/assets/images/highlight.svg' : '/assets/images/highlight_unclicked.svg'} onClick={() => {applyHighlight(); setIsHighlight(!isHighlight)}} />
    <img src='/assets/images/x.svg' onClick={() => {setIsTextStyleOpen(false)}} />
  </KeyboardToolbar>}
  {isListStyleOpen && <KeyboardToolbar style={{ bottom: `${keyboardHeight}px` }}>
    <div style={{display: 'flex', flexDirection: 'row', gap: '64px'}}>
        <img src={isList ? '/assets/images/list_tool_on.svg' : '/assets/images/list_tool_off.svg'} onClick={applyUnorderedList} />
    <img src={isNumber ? '/assets/images/number_tool_on.svg' : '/assets/images/number_tool_off.svg'} onClick={applyOrderedList} />
    </div>
    <img src='/assets/images/x.svg' onClick={() => {setIsListStyleOpen(false)}} />
  </KeyboardToolbar>}


    </Page>
  );
};

export default BubbleInputPage;

const Page = styled.div`
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
`;

const Header = styled.div`
  height: 40px;
  padding: 9px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

const Left = styled.div``;

const Right = styled.div`
  display: flex;
  gap: 32px;
`;

const MenuButton = styled.div`
  position: relative;
`;

const TitleInput = styled.input`
  font-size: 20px;
  font-weight: bold;
  color: #3A3D40;
  border: none;
  background: transparent;
  outline: none;
  width: 100%;
  margin-bottom: 8px;

  &::placeholder {
    color: #3A3D40;
    font-size: 20px;
    font-weight: bold;
  }
`;

const TextInput = styled.div`
  font-size: 18px;
  color: #3A3D40;
  border: none;
  background: transparent;
  outline: none;
  width: 100%;
  margin-bottom: 16px;

  &[contenteditable='true']:empty:before {
    content: attr(placeholder);
    color: #505458;
    font-weight: 500;
  }
`;


const ImagePreview = styled.img`
  max-width: 100%;
  border-radius: 8px;
  margin-bottom: 8px;
`;

const InsertImageButton = styled.button`
  background: none;
  border: 1px dashed #ccc;
  color: #666;
  font-size: 14px;
  padding: 8px 12px;
  cursor: pointer;
  margin-bottom: 24px;
`;

const KeyboardToolbar = styled.div`
  position: fixed;
  left: 0;
  right: 0;
  height: 24px;
  background-color: #fff;
  padding: 13px 22px;
  border-top: 1px solid #DEE2E7;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
//   gap: 16px;
  z-index: 1000;
  transition: bottom 0.2s ease;

  &::img {
  width: 24px;
  height: 24px;
  }
`;
