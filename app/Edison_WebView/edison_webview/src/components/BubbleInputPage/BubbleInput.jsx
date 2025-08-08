import React from 'react';
import styled from 'styled-components';

const white000 = '#ffffff';
const gray100 = '#f5f6f8';

const BubbleDoor = ({ outerColors = [], children }) => {
  const fullOuterGradient = getGradientColors(outerColors, true);  // white000 + outerColors
  const fullInnerGradient = getGradientColors(outerColors, false); // inner uses gray100 if empty

  const outerOpacity = outerColors.length === 0 ? 1 : 0.5;
  const innerOpacity = 0.5;

  return (
    <Container>
      <OuterLayer
        style={{
          background: fullOuterGradient,
          opacity: outerOpacity,
        }}
      />
      <InnerLayer
        style={{
          background: fullInnerGradient,
          opacity: innerOpacity,
        }}
      />
      <Content>{children}</Content>
    </Container>
  );
};

export default BubbleDoor;

function getGradientColors(colors, isOuter) {
  if (colors.length === 0) {
    return isOuter
      ? `linear-gradient(to bottom, ${white000}, ${gray100})`
      : gray100;
  }
  return `linear-gradient(to bottom, ${[white000, ...colors].join(', ')})`;
}

const Container = styled.div`
  position: fixed;
  bottom: 0;
  width: 100%;
  height: 90%;
  margin: 0 auto;
  overflow: auto;
`;

const OuterLayer = styled.div`
  position: absolute;
  inset: 0;
  border-radius: 200px 200px 0px 0px / 150px 150px 0px 0px;
  filter: blur(30px);
  z-index: 1;
`;

const InnerLayer = styled.div`
  position: absolute;
  inset: 24px; /* 더 작게 */
  border-radius: 180px 180px 0px 0px / 130px 130px 0px 0px;
  filter: blur(30px);
  z-index: 2;
`;

const Content = styled.div`
  position: relative;
  z-index: 3;
  padding: 117px 24px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  overflow-y: auto;
`;
