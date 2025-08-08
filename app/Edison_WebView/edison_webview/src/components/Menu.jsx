import React from 'react';
import styled from 'styled-components';

const Menu = ({ items }) => {
  return (
    <MenuContainer>
      {items.map((item, index) => (
        <React.Fragment key={index}>
          <MenuItem
            bgColor={item.bgColor}
            textColor={item.textColor}
            onClick={item.onClick}
          >
            {item.label}
          </MenuItem>
          {index < items.length - 1 && <Divider />}
        </React.Fragment>
      ))}
    </MenuContainer>
  );
};

export default Menu;

const MenuContainer = styled.ul`
  position: absolute;
  bottom: 100%; /* 아이콘 바로 아래 */
  left: 50%; /* 가로 중앙 */
  transform: translateX(-50%); /* 중앙 정렬 */
  list-style: none;
  padding: 0px 16px;
  margin-top: 8px;
  width: 118px;
  font-size: 16px;
  border: 1px solid #DEE2E7;
  border-radius: 16px;
  background-color: white;
  overflow: hidden;
  z-index: 9999;
  box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.1);
`;


const MenuItem = styled.li`
  padding: 12px 0px;
  background-color: ${({ bgColor }) => bgColor || 'none'};
  color: ${({ textColor }) => textColor};
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
`;

const Divider = styled.hr`
  width: 100%;
  height: 1px;
  background-color: #DEE2E7;
  border: none;
  margin: 0;
`;
