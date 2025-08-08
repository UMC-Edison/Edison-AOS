import React from 'react';
import styled from 'styled-components';

const ShareMenu = () => {
  return (
    <MenuContainer>
          <MenuItem
          >공유
            
          </MenuItem>
          <Divider />
          <MenuItem
           style={{color: '#FF0000'}}>삭제
            
          </MenuItem>
    </MenuContainer>
  );
};

export default ShareMenu;

const MenuContainer = styled.ul`
  position: absolute;
  bottom: 100px;
  right: 25%;
  list-style: none;
  padding: 0px 16px;
  margin: 0;
  width: 118px;
  font-size: 16px;
  border: 1px solid #DEE2E7;
  border-radius: 16px;
  overflow: hidden;
  z-index: 9999;
`;


const MenuItem = styled.li`
  padding: 12px 0px;
  background-color: #fff;
  color: #3A3D40;
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


