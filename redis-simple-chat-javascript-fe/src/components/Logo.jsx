const Logo = ({ width = 64, height = 64, bgColor = "#FDF2F8" }) => {
    return (
        <div
            style={{
                backgroundColor: bgColor,
                borderRadius: "50%",
                width: `${width}px`,
                height: `${height}px`,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                overflow: "hidden",
            }}
        >
            <img
                src="/insta_logo.png"
                alt="Logo"
                style={{
                    width: `${width * 1.8}px`,
                    height: `${height * 1.8}px`,
                    objectFit: "cover",
                }}
            />
        </div>
    );
};

export default Logo;
